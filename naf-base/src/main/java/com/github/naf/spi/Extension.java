package com.github.naf.spi;

import java.util.function.BiConsumer;

import org.jboss.weld.bootstrap.spi.Deployment;

public interface Extension extends javax.enterprise.inject.spi.Extension {
	default Deployment processDeployment(ApplicationContext applicationContext, Deployment deployment)
			throws RequirementException {
		return deployment;
	}

	default Resource transformResource(Object resource) {
		return null;
	}

	default void registerResource(BiConsumer<String, Resource> sink) {

	}

	default void join() {

	}

	default boolean with(Object o) {
		return false;
	}

}
