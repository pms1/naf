package com.github.naf.server.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.TypeLiteral;

import org.apache.sshd.common.Factory;
import org.apache.sshd.common.file.FileSystemAware;
import org.apache.sshd.server.AsyncCommand;
import org.apache.sshd.server.ChannelSessionAware;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.config.keys.DefaultAuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.contexts.AbstractBoundContext;
import org.jboss.weld.contexts.beanstore.MapBeanStore;
import org.jboss.weld.contexts.beanstore.NamingScheme;
import org.jboss.weld.contexts.beanstore.SimpleNamingScheme;
import org.jboss.weld.contexts.cache.RequestScopedCache;
import org.jboss.weld.manager.api.WeldManager;

import com.github.naf.server.ServerEndpointConfiguration;
import com.github.naf.spi.AfterBootEvent;
import com.github.naf.spi.ShutdownEvent;
import com.github.naf.spi.State;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.MapMaker;

public class NAFExtension implements com.github.naf.spi.Extension {

	class ForwardingCommand implements Command, SessionAware {
		Command delegate;

		ForwardingCommand(Command delegate) {
			Objects.requireNonNull(delegate);
			this.delegate = delegate;
		}

		@Override
		public void setInputStream(InputStream in) {
			try (CommandRequestScopeBinding t = associate(delegate)) {
				delegate.setInputStream(in);
			}
		}

		@Override
		public void setOutputStream(OutputStream out) {
			try (CommandRequestScopeBinding t = associate(delegate)) {
				delegate.setOutputStream(out);
			}
		}

		@Override
		public void setErrorStream(OutputStream err) {
			try (CommandRequestScopeBinding t = associate(delegate)) {
				delegate.setErrorStream(err);
			}
		}

		@Override
		public void setExitCallback(ExitCallback callback) {
			try (CommandRequestScopeBinding t = associate(delegate)) {
				delegate.setExitCallback(callback);
			}
		}

		@Override
		public void start(Environment env) throws IOException {
			try (CommandRequestScopeBinding t = associate(delegate)) {
				delegate.start(env);
			}
		}

		@Override
		public void destroy() throws Exception {
			try (CommandRequestScopeBinding t = associate(delegate)) {
				delegate.destroy();
			}
		}

		@Override
		public void setSession(ServerSession session) {
			try (CommandRequestScopeBinding t = associate(delegate)) {
				((SessionAware) delegate).setSession(session);
			}
		}

	}

	private SshServer sshd;

	private ServerEndpointConfiguration endpoint;

	@Override
	public boolean with(Object o) {
		if (o instanceof SshServerConfiguration) {
			endpoint = ((SshServerConfiguration) o).endpoint;
			Objects.requireNonNull(endpoint);
			Preconditions.checkArgument(endpoint.getPort() != 0);
			return true;
		}

		return false;
	}

	static class CommandContext {
		CommandContext(MyRequestContext requestContext, Map<String, Object> storage) {
			Objects.requireNonNull(requestContext);
			Objects.requireNonNull(storage);
			this.requestContext = requestContext;
			this.storage = storage;
		}

		final MyRequestContext requestContext;
		final Map<String, Object> storage;
		final AtomicInteger references = new AtomicInteger();
		final ThreadLocal<AtomicInteger> localReferences = ThreadLocal.withInitial(AtomicInteger::new);

		public CommandRequestScopeBinding attach() {
			requestContext.associate(storage);
			requestContext.activate();

			references.incrementAndGet();
			localReferences.get().incrementAndGet();

			return new CommandRequestScopeBinding(this);
		}

		public void detach() {
			int total = references.decrementAndGet();

			if (total == 0)
				requestContext.invalidate();

			if (localReferences.get().decrementAndGet() == 0) {
				requestContext.deactivate();
				requestContext.dissociate(storage);
			}

			if (total == 0)
				onDestroy.forEach(Runnable::run);
		}

		private final LinkedList<Runnable> onDestroy = new LinkedList<>();

		public void onDestroy(Runnable runnable) {
			onDestroy.addFirst(runnable);
		}
	}

	private final Map<Command, Optional<CommandContext>> commandContexts = new MapMaker().weakKeys().makeMap();

	private MyRequestContext requestContext;

	static class MyRequestContext extends AbstractBoundContext<Map<String, Object>> {

		protected MyRequestContext() {
			super(RegistrySingletonProvider.STATIC_INSTANCE, true);
		}

		@Override
		public Class<? extends Annotation> getScope() {
			return RequestScoped.class;
		}

		private final NamingScheme namingScheme = new SimpleNamingScheme("");

		@Override
		public boolean associate(Map<String, Object> storage) {
			if (getBeanStore() == null) {
				setBeanStore(new MapBeanStore(namingScheme, storage, true));
				getBeanStore().attach();
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void activate() {
			super.activate();
			RequestScopedCache.beginRequest();
		}

		@Override
		public void deactivate() {
			try {
				RequestScopedCache.endRequest();
			} finally {
				super.deactivate();
			}
		}

		@Override
		public void invalidate() {
			super.invalidate();
			getBeanStore().detach();
		}

	}

	CommandRequestScopeBinding associate(Command t) {
		Optional<CommandContext> optional = commandContexts.get(t);
		if (optional == null)
			throw new IllegalArgumentException("Command was not created through this ssh server");
		if (!optional.isPresent())
			throw new IllegalStateException("RequestScope for command " + t + " is already destroyed.");

		return optional.get().attach();
	}

	void registerContext(@Observes final AfterBeanDiscovery event) {
		event.addContext(requestContext = new MyRequestContext());
	}

	private <T> Command createCommandProxy(BeanManager bm, Bean<T> bean, Function<T, Command> create) {
		Map<String, Object> storage = new HashMap<>();
		CommandContext e = new CommandContext(requestContext, storage);
		try (CommandRequestScopeBinding t = e.attach()) {
			// see
			// http://stackoverflow.com/questions/20048410/canonical-way-to-obtain-cdi-managed-bean-instance-beanmanagergetreference-vs
			// on why this is the appropriate way
			@SuppressWarnings("unchecked")
			T instance = (T) bm.getReference(bean, bean.getBeanClass(), bm.createCreationalContext(bean));

			Command commandInstance = create.apply(instance);

			commandContexts.put(commandInstance, Optional.of(e));
			e.onDestroy(() -> commandContexts.put(commandInstance, Optional.empty()));

			// perform injection on created command
			InjectionTarget<Object> it;
			try {
				it = (InjectionTarget<Object>) cache.get(commandInstance.getClass());
			} catch (ExecutionException e2) {
				throw new RuntimeException(e2);
			}
			CreationalContext<Object> cc = manager.createCreationalContext(null);
			it.inject(commandInstance, cc);
			e.onDestroy(() -> {
				InjectionTarget<Object> it1;
				try {
					it1 = (InjectionTarget<Object>) cache.get(commandInstance.getClass());
				} catch (ExecutionException e1) {
					throw new RuntimeException(e1);
				}
				it1.dispose(commandInstance);
				cc.release();
			});

			// explicit reference for the lifecycle of the command itself
			e.references.incrementAndGet();

			ForwardingCommand cmd = new ForwardingCommand(commandInstance) {
				public void destroy() throws Exception {
					// remove reference of command lifecycle
					e.references.decrementAndGet();

					super.destroy();
				};
			};

			List<Class<?>> interfaces = new LinkedList<>();
			interfaces.add(Command.class);
			if (commandInstance instanceof SessionAware)
				interfaces.add(SessionAware.class);
			if (commandInstance instanceof ChannelSessionAware)
				interfaces.add(ChannelSessionAware.class);
			if (commandInstance instanceof FileSystemAware)
				interfaces.add(FileSystemAware.class);
			if (commandInstance instanceof AsyncCommand)
				interfaces.add(AsyncCommand.class);
			return (Command) Proxy.newProxyInstance(commandInstance.getClass().getClassLoader(),
					interfaces.toArray(new Class[0]), (proxy, method, args) -> {
						try {
							return method.invoke(cmd, args);
						} catch (InvocationTargetException e1) {
							throw e1.getCause();
						} catch (IllegalAccessException | IllegalArgumentException e1) {
							throw new Error(e1);
						}
					});
		}
	}

	@SuppressWarnings("serial")
	static final Type factoryCommandType = new TypeLiteral<Factory<Command>>() {
	}.getType();

	private WeldManager manager;
	private LoadingCache<Class<?>, InjectionTarget<?>> cache;

	void afterBoot(@Observes AfterBootEvent afterBootEvent, BeanManager bm, @State Path stateDirectory)
			throws IOException {
		if (endpoint == null)
			return;

		this.manager = (WeldManager) bm;
		this.cache = CacheBuilder.newBuilder().weakValues().build(new CacheLoader<Class<?>, InjectionTarget<?>>() {
			@Override
			public InjectionTarget<?> load(Class<?> key) throws Exception {
				AnnotatedType<?> type = manager.createAnnotatedType(key);
				return manager.createInjectionTargetBuilder(type).setResourceInjectionEnabled(false)
						.setTargetClassLifecycleCallbacksEnabled(false).build();
			}
		});

		SshServer sshd = SshServer.setUpDefaultServer();
		if (endpoint.getAddress() != null)
			sshd.setHost(endpoint.getAddress().getHostAddress());
		sshd.setPort(endpoint.getPort());

		Files.createDirectories(stateDirectory);
		AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider(
				stateDirectory.resolve("hostkey.ser"));
		hostKeyProvider.setAlgorithm("RSA");
		sshd.setKeyPairProvider(hostKeyProvider);

		sshd.setPublickeyAuthenticator(new DefaultAuthorizedKeysAuthenticator(true));

		@SuppressWarnings("unchecked")
		Bean<CommandFactory> cfBean = (Bean<CommandFactory>) bm.resolve(bm.getBeans(CommandFactory.class));
		if (cfBean != null)
			sshd.setCommandFactory((command) -> createCommandProxy(bm, cfBean,
					commandFactory -> commandFactory.createCommand(command)));

		@SuppressWarnings("unchecked")
		Bean<Factory<Command>> shellBean = (Bean<Factory<Command>>) bm.resolve(bm.getBeans(factoryCommandType));
		if (shellBean != null)
			sshd.setShellFactory(() -> createCommandProxy(bm, shellBean, Factory::create));

		sshd.start();
		this.sshd = sshd;
	}

	@Override
	public void join() {
		if (sshd != null) {
			Object o = new Object();
			synchronized (o) {
				try {
					o.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	void shutdown(@Observes ShutdownEvent shutdownEvent) {
		if (sshd != null)
			try {
				sshd.stop();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
