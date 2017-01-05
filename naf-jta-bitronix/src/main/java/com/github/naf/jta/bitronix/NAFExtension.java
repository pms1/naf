package com.github.naf.jta.bitronix;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.transaction.TransactionScoped;

import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.transaction.spi.TransactionServices;

import com.github.naf.jta.XADataSourceFactory;
import com.github.naf.spi.ApplicationContext;
import com.github.naf.spi.Extension;
import com.github.naf.spi.RequirementException;
import com.github.naf.spi.Resource;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class NAFExtension implements Extension {

	@Override
	public Resource transformResource(Object resource) {

		if (resource instanceof XADataSourceFactory) {
			XADataSourceFactory f = (XADataSourceFactory) resource;

			PoolingDataSource result = new PoolingDataSource();
			result.setClassName(f.className);
			result.setUniqueName(f.id);
			if (f.minPool != null)
				result.setMinPoolSize(f.minPool);
			if (f.maxPool != null)
				result.setMaxPoolSize(f.maxPool);
			if (f.allowLocalTransactions != null)
				result.setAllowLocalTransactions(f.allowLocalTransactions);
			result.getDriverProperties().putAll(f.properties);

			return new Resource() {

				@Override
				public Object getValue() {
					return result;
				}

				@Override
				public void close() {
					result.close();
				}
			};
		}

		return Extension.super.transformResource(resource);
	}

	@Override
	public Deployment processDeployment(ApplicationContext applicationContext, Deployment deployment)
			throws RequirementException {
		deployment.getServices().add(TransactionServices.class, new BitronixTransactionServices());

		return Extension.super.processDeployment(applicationContext, deployment);
	}

	void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {
		bbd.addScope(TransactionScoped.class, true, true);
	}

	void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
		abd.addContext(new TransactionContext());
	}

}
