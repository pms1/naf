package com.github.naf.server.servlet.jaxrs;

import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.ws.rs.ext.Provider;

import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.servlet.ServletContainer;

import com.github.naf.server.servlet.ServletInitializationEvent;
import com.github.naf.spi.Extension;

public class NAFExtension implements Extension {
	void init(@Observes ServletInitializationEvent event, BeanManager bm) {
		ServletHolder jerseyServlet = event.getContext().addServlet(ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);

		// Tells the Jersey servlet to load all classes annotated with
		// "@Path" or "@Provider".
		jerseyServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES,
				bm.getBeans(Object.class).stream()//
						.map(b -> b.getBeanClass()) //
						.filter(c -> Resource.getPath(c) != null || c.isAnnotationPresent(Provider.class)) //
						.map(c -> c.getCanonicalName()) //
						.collect(Collectors.joining(" ")));

		// suppress "Content-Length" HTTP header to allow streaming output
		jerseyServlet.setInitParameter(ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, "0");
	}
}
