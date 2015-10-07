package com.github.naf.server.servlet.jaxrs;

import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;

import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.Resource;

import com.github.naf.server.servlet.ServletInitializationEvent;
import com.github.naf.spi.Extension;

public class NAFExtension implements Extension {
	void init(@Observes ServletInitializationEvent event, BeanManager bm) {
		ServletHolder jerseyServlet = event.getContext().addServlet(org.glassfish.jersey.servlet.ServletContainer.class,
				"/*");
		jerseyServlet.setInitOrder(0);

		// Tells the Jersey servlet to load all classes annotated with
		// "@Path"
		jerseyServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES,
				bm.getBeans(Object.class).stream()//
						.map(b -> b.getBeanClass()) //
						.filter(c -> Resource.getPath(c) != null) //
						.map(c -> c.getCanonicalName()) //
						.collect(Collectors.joining(" ")));

		System.err.println("PARAMS " + jerseyServlet.getInitParameter(ServerProperties.PROVIDER_CLASSNAMES));

	}
}
