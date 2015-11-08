package com.github.naf.server.servlet;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.weld.environment.servlet.Listener;

import test.AfterBootEvent;
import test.ShutdownEvent;

public class NAFExtension implements com.github.naf.spi.Extension {
	private Server jettyServer;

	void afterBoot(@Observes AfterBootEvent afterBootEvent, BeanManager bm, Event<ServletInitializationEvent> event) {

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		jettyServer = new Server(8080);
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
