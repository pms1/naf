package com.github.naf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.logging.LogManager;

import javax.ejb.TimerService;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.ejb.spi.InterceptorBindings;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.spi.ResourceLoader;

import com.github.naf.spi.ApplicationContext;
import com.github.naf.spi.Extension;
import com.github.naf.spi.RequirementException;
import com.github.naf.spi.Resource;
import com.google.common.collect.Iterables;

public class ApplicationBuilder {
	public ApplicationBuilder() {

	}

	private Map<String, Object> resources = new HashMap<>();

	private String[] args;

	public ApplicationBuilder withResource(String id, Object o) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(o);
		Object old = resources.putIfAbsent(id, o);
		if (old != null)
			throw new IllegalArgumentException();
		return this;
	}

	private List<Object> with = new LinkedList<>();

	public ApplicationBuilder with(Object o) {
		Objects.requireNonNull(o);
		if (with.contains(o))
			throw new IllegalArgumentException();
		with.add(o);
		return this;
	}

	/**
	 * Make {@code args} available for injection using Weld's {@link Parameters}
	 * annotation. If this method is not called, {@link Parameters} is not
	 * available for injection.
	 * 
	 * @param args
	 *            The command line arguments. Must not be {@code null}.
	 * @return this
	 */
	public ApplicationBuilder withParameters(String[] args) {
		Objects.requireNonNull(args);
		this.args = args;
		return this;
	}

	public static class X implements InitialContextFactoryBuilder {
		private ApplicationContext ac;

		void setApplicationContext(ApplicationContext ac) {
			if ((ac != null) == (this.ac != null))
				throw new IllegalStateException();
			this.ac = ac;
		}

		public X() {
		}

		@Override
		public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
			return new InitialContextFactory() {

				@Override
				public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
					return new Context() {

						@Override
						public Object lookup(Name name) throws NamingException {
							return lookup(name.toString());
						}

						@Override
						public Object lookup(String name) throws NamingException {
							Object resource = ac.getResource(name);
							if (resource == null)
								throw new NameNotFoundException("resource " + name + " not found");
							return resource;
						}

						@Override
						public void bind(Name name, Object obj) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public void bind(String name, Object obj) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public void rebind(Name name, Object obj) throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public void rebind(String name, Object obj) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public void unbind(Name name) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public void unbind(String name) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public void rename(Name oldName, Name newName) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public void rename(String oldName, String newName) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public void destroySubcontext(Name name) throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public void destroySubcontext(String name) throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public Context createSubcontext(Name name) throws NamingException {
							throw new OperationNotSupportedException();
						}

						@Override
						public Context createSubcontext(String name) throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public Object lookupLink(Name name) throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public Object lookupLink(String name) throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public NameParser getNameParser(Name name) throws NamingException {
							return new BitronixNameParser();

						}

						@Override
						public NameParser getNameParser(String name) throws NamingException {
							return new BitronixNameParser();
						}

						@Override
						public Name composeName(Name name, Name prefix) throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public String composeName(String name, String prefix) throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public Object addToEnvironment(String propName, Object propVal) throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public Object removeFromEnvironment(String propName) throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public Hashtable<?, ?> getEnvironment() throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public void close() throws NamingException {
							throw new OperationNotSupportedException();

						}

						@Override
						public String getNameInNamespace() throws NamingException {
							throw new OperationNotSupportedException();

						}

					};
				}
			};
		}

	}

	private final static class BitronixNameParser implements NameParser {
		private static final BitronixNameParser INSTANCE = new BitronixNameParser();

		public Name parse(final String name) throws NamingException {
			return new CompositeName(name);
		}
	}

	static class Holder {
		static X namingManager;

		static {
			try {
				X x = new X();
				NamingManager.setInitialContextFactoryBuilder(x);
				namingManager = x;
			} catch (IllegalStateException | NamingException e1) {
				throw new Error("failed to set InitialContextFactoryBuilder");
			}
		}
	}

	public Application build() {
		if (true)
			try {
				LogManager.getLogManager()
						.readConfiguration(new ByteArrayInputStream(( //
								"handlers = java.util.logging.ConsoleHandler\r\n" //
										//
										+ "java.util.logging.ConsoleHandler.level = ALL\r\n" //
										+ ".level = INFO\r\n" //
										+ "org.hibernate.level = WARNING\r\n" //
										+ "XXorg.hibernate.level = FINEST\r\n" //
										+ "XXorg.hibernate.SQL.level = FINEST\r\n" //
										+ "XXorg.hibernate.integrator.level = FINEST\r\n" //
										+ "bitronix.tm.level = WARNING\r\n" //
										+ "org.jboss.weld.level = WARNING\r\n" //
										+ "org.eclipse.jetty.level = WARNING\r\n" //
				// + "org.glassfish.jersey.level=FINEST\r\n" //
										+ "org.glassfish.jersey.server.model.level=WARNING\r\n" //
				// + "com.github.naf.jta.level=FINEST\r\n" //
				// + ".level=FINEST\r\n" //
				).getBytes()));
			} catch (SecurityException | IOException e1) {
				throw new RuntimeException(e1);
			}

		ServiceLoader<Extension> extensions = ServiceLoader.load(Extension.class);

		List<javax.enterprise.inject.spi.Extension> manualExtensions = new LinkedList<>();

		for (Object o : with) {
			boolean handled = false;
			if (o instanceof javax.enterprise.inject.spi.Extension) {
				manualExtensions.add((javax.enterprise.inject.spi.Extension) o);
				handled = true;
			}
			for (Extension e : extensions) {
				if (e.with(o)) {
					handled = true;
					break;
				}
			}
			if (!handled) {
				throw new IllegalStateException("Cannot handle resource: " + o);
			}
		}

		Map<String, Resource> transformedResources = new HashMap<>();
		for (Map.Entry<String, Object> r : resources.entrySet()) {
			List<Resource> cand = new LinkedList<>();

			for (Extension e : extensions) {
				Resource newResource = e.transformResource(r.getValue());
				if (newResource != null)
					cand.add(newResource);
			}

			switch (cand.size()) {
			case 0:
				transformedResources.put(r.getKey(), () -> r.getValue());
				break;
			case 1:
				transformedResources.put(r.getKey(), Iterables.getOnlyElement(cand));
				break;
			default:
				throw new IllegalStateException();
			}
		}

		ApplicationContext ac = new ApplicationContext() {

			@Override
			public Object getResource(String id) {
				return transformedResources.get(id).getValue();
			}

		};

		Holder.namingManager.setApplicationContext(ac);

		ResourceInjectionServicesImpl ris = new ResourceInjectionServicesImpl();

		Weld weld = new Weld() {
			@Override
			protected Deployment createDeployment(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
				Deployment deployment = super.createDeployment(resourceLoader, bootstrap);

				List<Extension> todo = new LinkedList<>();
				Iterables.addAll(todo, extensions);
				for (;;) {
					List<Extension> again = new LinkedList<>();
					List<String> texts = new LinkedList<>();
					for (Extension e : todo) {
						try {
							deployment = e.processDeployment(ac, deployment);
						} catch (RequirementException e1) {
							again.add(e);
							texts.add(e1.toString());
						}
					}
					if (again.isEmpty())
						break;
					else if (again.size() == todo.size())
						throw new Error("Unresolved requirements: " + texts);
					else
						todo = again;
				}

				deployment.getServices().add(EjbServices.class, new FakeEjbServices());
				deployment.getServices().add(ResourceInjectionServices.class, ris);

				return deployment;
			}
		};

		for (Extension e : extensions)
			weld = weld.addExtension(e);
		for (javax.enterprise.inject.spi.Extension e : manualExtensions)
			weld = weld.addExtension(e);

		if (args != null)
			weld = weld.addExtension(new ParametersExtension(args));
		else
			weld = weld.addExtension(new ParametersExtension());

		WeldContainer container = weld.initialize();

		Application a = new ApplicationImpl(ac, weld, container, extensions, () -> {
			transformedResources.values().forEach((r) -> r.close());
		});

		ris.setBeanManager(container.getBeanManager());

		return a;
	}

	static class FakeEjbServices implements EjbServices {

		@Override
		public void cleanup() {
			// throw new Error();
		}

		@Override
		public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor) {
			throw new Error();
		}

		@Override
		public void registerInterceptors(EjbDescriptor<?> ejbDescriptor, InterceptorBindings interceptorBindings) {
			throw new Error();

		}

	}

	static class ResourceInjectionServicesImpl implements ResourceInjectionServices {

		@Override
		public void cleanup() {

		}

		BeanManagerImpl bm;

		public void setBeanManager(BeanManager beanManager) {
			bm = (BeanManagerImpl) beanManager;
		}

		@Override
		public ResourceReferenceFactory<Object> registerResourceInjectionPoint(InjectionPoint injectionPoint) {

			if (injectionPoint.getType().equals(TimerService.class)) {

				return new ResourceReferenceFactory<Object>() {

					@Override
					public ResourceReference<Object> createResource() {

						return new ResourceReference<Object>() {

							@Override
							public Object getInstance() {
								// return new TimerServiceImpl(injectionPoint,
								// bm);
								throw new Error();
							}

							@Override
							public void release() {
								// TODO Auto-generated method stub

							}
						};
					}
				};
			}

			System.err.println("IP " + injectionPoint);
			throw new Error("ip " + injectionPoint);
		}

		@Override
		public ResourceReferenceFactory<Object> registerResourceInjectionPoint(String jndiName, String mappedName) {
			throw new Error();
		}

		@Override
		public Object resolveResource(InjectionPoint injectionPoint) {
			throw new Error();
		}

		@Override
		public Object resolveResource(String jndiName, String mappedName) {
			throw new Error();
		}

	}
}
