package com.github.naf.samples.jpa;

import org.postgresql.xa.PGXADataSource;

import com.github.naf.jta.XADataSourceFactory;

import test.Application;
import test.ApplicationBuilder;

public class JpaSampleMain {
	public static void main(String[] args) {

		Object ds = new XADataSourceFactory().setClassName(org.h2.jdbcx.JdbcDataSource.class.getName()) //
				.setAllowLocalTransactions(false) //
				.setUniqueName("xxx") //
				.setPoolSize(1, 5) //
				.addProperty("user", "sa") //
				.addProperty("password", "sa") //
				.addProperty("url", "jdbc:h2:mem:" + "test" + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");

		if (false)
			ds = new XADataSourceFactory().setClassName(PGXADataSource.class.getName()) //
					.setUniqueName("test") //
					.setPoolSize(1, 5) //
					.addProperty("user", "Mirko") //
					.addProperty("password", "") //
					.addProperty("url", "jdbc:postgresql://alice/test");

		try (Application a = new ApplicationBuilder() //
				.withResource("java:comp/env/jdbc/test", ds) //
				.build()) {
			a.get(JpaHelloService.class).hello();
			a.get(JpaHelloService.class).hello2();

			JpaHelloService service = a.get(JpaHelloService.class);
			for (int i = 0; i != 10; ++i)
				service.hello();
		}
	}
}
