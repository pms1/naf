package com.github.naf.server.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Objects;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
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
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.Unbound;

import com.github.naf.spi.AfterBootEvent;
import com.github.naf.spi.ShutdownEvent;

public class NAFExtension implements com.github.naf.spi.Extension {

	static class ForwardingCommand implements Command {
		Command delegate;

		ForwardingCommand(Command delegate) {
			Objects.requireNonNull(delegate);
			this.delegate = delegate;
		}

		@Override
		public void setInputStream(InputStream in) {
			delegate.setInputStream(in);
		}

		@Override
		public void setOutputStream(OutputStream out) {
			delegate.setOutputStream(out);
		}

		@Override
		public void setErrorStream(OutputStream err) {
			delegate.setErrorStream(err);
		}

		@Override
		public void setExitCallback(ExitCallback callback) {
			delegate.setExitCallback(callback);
		}

		@Override
		public void start(Environment env) throws IOException {
			delegate.start(env);
		}

		@Override
		public void destroy() {
			delegate.destroy();
		}

	}

	private SshServer sshd;

	@SuppressWarnings("serial")
	void afterBoot(@Observes AfterBootEvent afterBootEvent, BeanManager bm, @Unbound RequestContext requestContext)
			throws IOException {

		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(5222);

		AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider(
				Paths.get("c:/temp/ssh/foo"));
		hostKeyProvider.setAlgorithm("RSA");
		sshd.setKeyPairProvider(hostKeyProvider);

		sshd.setPublickeyAuthenticator(new DefaultAuthorizedKeysAuthenticator(false));

		// sshd.setPasswordAuthenticator(new InAppPasswordAuthenticator());
		// sshd.setShellFactory(new InAppShellFactory());

		@SuppressWarnings("unchecked")
		Bean<CommandFactory> cfBean = (Bean<CommandFactory>) bm.resolve(bm.getBeans(CommandFactory.class));
		if (cfBean != null)
			sshd.setCommandFactory((command) -> {
				requestContext.activate();
				try {
					CreationalContext<CommandFactory> context = bm.createCreationalContext(cfBean);
					CommandFactory instance = cfBean.create(context);
					return new ForwardingCommand(instance.createCommand(command)) {
						public void destroy() {
							try {
								super.destroy();
							} finally {
								cfBean.destroy(instance, context);
								context.release();
							}
						};
					};
				} finally {
					requestContext.deactivate();
				}
			});

		@SuppressWarnings("unchecked")
		Bean<Factory<Command>> shellBean = (Bean<Factory<Command>>) bm
				.resolve(bm.getBeans(new TypeLiteral<Factory<Command>>() {
				}.getType()));
		if (shellBean != null)
			sshd.setShellFactory(() -> {
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
							}
						};
					};
				} finally {
					requestContext.deactivate();
				}
			});

		System.err.println("C1 " + sshd.getChannelFactories());
		System.err.println("C1 " + sshd.getCommandFactory());
		System.err.println("C1 " + sshd.getServiceFactories());
		System.err.println("C1 " + sshd.getSessionFactory());
		System.err.println("C1 " + sshd.getShellFactory());
		System.err.println("C1 " + sshd.getSubsystemFactories());

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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
