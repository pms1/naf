package com.github.naf.spi;

import org.jboss.weld.bootstrap.spi.Deployment;

public interface Extension extends javax.enterprise.inject.spi.Extension {
	default Deployment processDeployment(ApplicationContext applicationContext, Deployment deployment) {
		return deployment;
	}

	default Object transformResource(Object resource) {
		return null;
	}

	default void join() {

	}

	default boolean with(Object o) {
		return false;
	}

}
