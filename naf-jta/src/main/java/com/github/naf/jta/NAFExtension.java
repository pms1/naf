package com.github.naf.jta;

import java.util.ServiceLoader;

import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.transaction.spi.TransactionServices;

import com.github.naf.spi.ApplicationContext;
import com.github.naf.spi.Extension;
import com.google.common.collect.Iterators;

public class NAFExtension implements Extension {

	@Override
	public Deployment processDeployment(ApplicationContext ac, Deployment deployment) {
		System.err.println("XX-ROOT-JTA");

		deployment.getServices().add(TransactionServices.class,
				Iterators.getOnlyElement(ServiceLoader.load(TransactionServices.class).iterator()));

		return Extension.super.processDeployment(ac, deployment);
	}
}
