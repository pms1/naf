package test;

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
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.resources.spi.ResourceLoader;

import com.github.naf.spi.Extension;
import com.google.common.collect.Iterables;

public class ApplicationBuilder {
	public ApplicationBuilder() {

	}

	private Map<String, Object> resources = new HashMap<>();

	public ApplicationBuilder withResource(String id, Object o) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(o);
		Object old = resources.putIfAbsent(id, o);
		if (old != null)
			throw new IllegalArgumentException();
		return this;
	}

	public static class X implements InitialContextFactoryBuilder {
		final ApplicationContext ac;

		public X(ApplicationContext ac) {
			Objects.requireNonNull(ac);
			this.ac = ac;
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

	public Application build() {
		if (true)
			try {
				LogManager.getLogManager()
						.readConfiguration(new ByteArrayInputStream(
								"handlers = java.util.logging.ConsoleHandler\r\njava.util.logging.ConsoleHandler.level = ALL\r\n.level = FINE\r\n"
										.getBytes()));
			} catch (SecurityException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		ServiceLoader<Extension> extensions = ServiceLoader.load(Extension.class);
		Map<String, Object> transformedResources = new HashMap<>();
		for (Map.Entry<String, Object> r : resources.entrySet()) {
			List<Object> cand = new LinkedList<>();

			for (Extension e : extensions) {
				Object newResource = e.transformResource(r.getValue());
				if (newResource != null)
					cand.add(newResource);
			}

			switch (cand.size()) {
			case 0:
				transformedResources.put(r.getKey(), r.getValue());
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
				return transformedResources.get(id);
			}

		};

		try {
			NamingManager.setInitialContextFactoryBuilder(new X(ac));
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Weld weld = new Weld() {
			@Override
			protected Deployment createDeployment(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
				Deployment deployment = super.createDeployment(resourceLoader, bootstrap);

				for (Extension e : extensions)
					deployment = e.processDeployment(ac, deployment);

				return deployment;
			}
		};

		for (Extension e : extensions)
			weld = weld.addExtension(e);

		Application a = new Application(weld, extensions);

		return a;
	}
}
