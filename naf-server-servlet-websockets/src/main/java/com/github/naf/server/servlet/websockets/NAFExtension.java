package com.github.naf.server.servlet.websockets;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.servlet.ServletException;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import com.github.naf.server.servlet.ServletInitializationEvent;
import com.github.naf.spi.Extension;

public class NAFExtension implements Extension {

	void init(@Observes ServletInitializationEvent event, BeanManager bm) throws ServletException {

		// Initialize javax.websocket layer
		org.eclipse.jetty.websocket.jsr356.server.ServerContainer wscontainer = WebSocketServerContainerInitializer
				.configureContext(event.getContext());

		bm.getBeans(Object.class).stream()//
				.map(b -> b.getBeanClass()) //
				.filter(c -> c.isAnnotationPresent(ServerEndpoint.class)) //
				.forEach(c -> {

					try {
						wscontainer.addEndpoint(c);
					} catch (Exception e) {
						throw new Error(e);
					}
				});
		//
		// });
	}

	<X> void process(@Observes ProcessAnnotatedType<X> pat) {
		System.err.println("PATPAT " + pat);
	}
}
