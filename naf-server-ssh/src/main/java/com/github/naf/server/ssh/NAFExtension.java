package com.github.naf.server.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.TypeLiteral;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.config.keys.DefaultAuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.MapBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.cache.RequestScopedCache;

import com.github.naf.server.ServerEndpointConfiguration;
import com.github.naf.spi.AfterBootEvent;
import com.github.naf.spi.ShutdownEvent;
import com.github.naf.spi.State;
import com.google.common.base.Preconditions;

public class NAFExtension implements com.github.naf.spi.Extension {

	class ForwardingCommand implements Command {
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
		public void destroy() {
			try (CommandRequestScopeBinding t = associate(delegate)) {
				delegate.destroy();
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

	private Map<Command, CommandContext> commandContexts = new HashMap<>();

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

	private CommandContext getCommandContext(Command t) {
		return commandContexts.get(t);
	}

	CommandRequestScopeBinding associate(Command t) {
		CommandContext commandContext = getCommandContext(t);
		if (commandContext == null)
			throw new IllegalStateException("No RequestScope for command " + t + ".");
		return commandContext.attach();
	}

	public void registerContext(@Observes final AfterBeanDiscovery event) {
		event.addContext(requestContext = new MyRequestContext());
	}

	@SuppressWarnings("serial")
	void afterBoot(@Observes AfterBootEvent afterBootEvent, BeanManager bm, @State Path p) throws IOException {
		Objects.requireNonNull(endpoint);

		SshServer sshd = SshServer.setUpDefaultServer();
		if (endpoint.getAddress() != null)
			sshd.setHost(endpoint.getAddress().getHostAddress());
		sshd.setPort(endpoint.getPort());

		Files.createDirectories(p);
		AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider(p.resolve("hostkey.ser"));
		hostKeyProvider.setAlgorithm("RSA");
		sshd.setKeyPairProvider(hostKeyProvider);

		sshd.setPublickeyAuthenticator(new DefaultAuthorizedKeysAuthenticator(true));

		@SuppressWarnings("unchecked")
		Bean<CommandFactory> cfBean = (Bean<CommandFactory>) bm.resolve(bm.getBeans(CommandFactory.class));
		if (cfBean != null)
			sshd.setCommandFactory((command) -> {
				Map<String, Object> storage = new HashMap<>();
				CommandContext e = new CommandContext(requestContext, storage);
				try (CommandRequestScopeBinding t = e.attach()) {
					CreationalContext<CommandFactory> context = bm.createCreationalContext(cfBean);
					e.onDestroy(() -> context.release());
					CommandFactory instance = cfBean.create(context);
					e.onDestroy(() -> cfBean.destroy(instance, context));

					Command commandInstance = instance.createCommand(command);

					commandContexts.put(commandInstance, e);
					e.onDestroy(() -> commandContexts.remove(commandInstance));

					// if (command instanceof SessionAware) {
					// if (command instanceof ChannelSessionAware) {
					// if (command instanceof FileSystemAware) {
					// if (command instanceof AsyncCommand) {

					// explicit reference for the lifecycle of the command
					// itself
					e.references.incrementAndGet();

					return new ForwardingCommand(commandInstance) {
						public void destroy() {
							// remove reference of command lifecycle
							e.references.decrementAndGet();

							super.destroy();
						};
					};
				}
			});

		@SuppressWarnings("unchecked")
		Bean<Factory<Command>> shellBean = (Bean<Factory<Command>>) bm
				.resolve(bm.getBeans(new TypeLiteral<Factory<Command>>() {
				}.getType()));
		if (shellBean != null)
			sshd.setShellFactory(() ->

			{
				requestContext.activate();
				try {
					CreationalContext<Factory<Command>> context = bm.createCreationalContext(shellBean);
					Factory<Command> instance = shellBean.create(context);
					return new ForwardingCommand(instance.create()) {
						public void destroy() {
							try {
								super.destroy();
							} finally {
								shellBean.destroy(instance, context);
								context.release();
								requestContext.invalidate();
							}
						};
					};
				} finally {
					requestContext.deactivate();
				}
			});

		sshd.start();
		this.sshd = sshd;
	}

	@Override
	public void join() {
		Object o = new Object();
		synchronized (o) {
			try {
				o.wait();
			} catch (InterruptedException e) {
			}
		}

	}

	void shutdown(@Observes ShutdownEvent shutdownEvent) {
		System.err.println("STOP " + sshd);
		if (sshd != null)
			try {
				sshd.stop();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
