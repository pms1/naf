package com.github.naf.spi;

import org.jboss.weld.bootstrap.spi.Deployment;

import test.ApplicationContext;

public interface Extension {
	default Deployment processDeployment(ApplicationContext applicationContext, Deployment deployment) {
		return deployment;
	}

	default Object transformResource(Object resource) {
		return null;
	}
}
