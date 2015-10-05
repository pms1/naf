package com.github.naf.jta.bitronix;

import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

import bitronix.tm.resource.jdbc.PoolingDataSource;

@ApplicationScoped
public class BitronixDataSourceFactory implements dq.platform.jpa.DataSourceFactory {
	public Builder create() {
		return new BuilderImpl();
	}

	public static class BuilderImpl implements dq.platform.jpa.DataSourceFactory.Builder {
		private PoolingDataSource myDataSource = new PoolingDataSource();

		private BuilderImpl() {
		}

		public Builder setClassName(String className) {
			myDataSource.setClassName(className);
			return this;
		}

		public Builder setUniqueName(String uniqueName) {
			myDataSource.setUniqueName(uniqueName);
			return this;
		}

		public Builder setPoolSize(int min, int max) {
			myDataSource.setMinPoolSize(min);
			myDataSource.setMaxPoolSize(max);
			return this;
		}

		public Builder addProperty(String key, String value) {
			myDataSource.getDriverProperties().setProperty(key, value);
			return this;
		}

		public DataSource build() {
			myDataSource.init();
			return myDataSource;
		}

		@Override
		public Builder setAllowLocalTransactions(boolean allowLocalTransaction) {
			myDataSource.setAllowLocalTransactions(allowLocalTransaction);
			return this;
		}

	}
}
