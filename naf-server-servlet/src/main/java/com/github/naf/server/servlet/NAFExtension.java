package com.github.naf.server.servlet;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.weld.environment.servlet.Listener;

import com.github.naf.server.ServerEndpointConfiguration;
import com.github.naf.spi.AfterBootEvent;
import com.github.naf.spi.ShutdownEvent;

public class NAFExtension implements com.github.naf.spi.Extension {
	private Server jettyServer;

	private ServerEndpointConfiguration endpoint = new ServerEndpointConfiguration();

	@Override
	public boolean with(Object o) {
		if (o instanceof ServletServerConfiguration) {
			this.endpoint = ((ServletServerConfiguration) o).endpoint;
			Objects.requireNonNull(this.endpoint);
			return true;
		}
		return false;
	}

	static class Producer {

		@Inject
		BeanManager bm;

		@Produces
		URI uri() {
			NAFExtension e = bm.getExtension(NAFExtension.class);

			Connector[] connectors = e.jettyServer.getConnectors();
			if (connectors.length != 1)
				throw new IllegalStateException();
			ServerConnector sc = (ServerConnector) connectors[0];

			int port = sc.getLocalPort();
			if (port < 0)
				throw new IllegalStateException();
			String host = sc.getHost();
			if (host == null)
				throw new IllegalStateException();

			try {
				return new URI("http", null, host, port, contextPath, null, null);
			} catch (URISyntaxException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	private static final String contextPath = "/";

	void afterBoot(@Observes AfterBootEvent afterBootEvent, BeanManager bm, Event<ServletInitializationEvent> event) {
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(contextPath);

		jettyServer = new Server(new InetSocketAddress(endpoint.getAddress(), endpoint.getPort()));
		jettyServer.setHandler(context);
		context.addEventListener(Listener.using(bm));

		event.fire(new ServletInitializationEvent(context));

		try {
			jettyServer.start();
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	@Override
	public void join() {
		try {
			jettyServer.join();
		} catch (InterruptedException e) {
			throw new Error(e);
		}
	}

	void shutdown(@Observes ShutdownEvent shutdownEvent) {
		if (jettyServer == null)
			return;
		// always try to shutdown (require at least on isRunning and FAILED;
		// jetty is ignoring duplicate stop() gracefully)
		try {
			jettyServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
			// throw new Error(e);
		}
	}
}
