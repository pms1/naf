package com.github.naf.jta.bitronix;

import com.github.naf.jta.XADataSourceFactory;
import com.github.naf.spi.Extension;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class NAFExtension implements Extension {

	@Override
	public Object transformResource(Object resource) {

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
			return result;
		}

		// TODO Auto-generated method stub
		return Extension.super.transformResource(resource);
	}
}
