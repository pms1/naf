package com.github.naf.jta;

import org.jboss.weld.bootstrap.spi.Deployment;

import com.github.naf.spi.ApplicationContext;
import com.github.naf.spi.Extension;
import com.github.naf.spi.RequirementException;

public class NAFExtension implements Extension {

	@Override
	public Deployment processDeployment(ApplicationContext ac, Deployment deployment) throws RequirementException {
		return Extension.super.processDeployment(ac, deployment);
	}
}
