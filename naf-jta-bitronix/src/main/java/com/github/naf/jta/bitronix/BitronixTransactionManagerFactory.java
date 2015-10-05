package com.github.naf.jta.bitronix;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.transaction.TransactionManager;

import bitronix.tm.TransactionManagerServices;

@ApplicationScoped
public class BitronixTransactionManagerFactory {
	@Produces
	@Singleton
	TransactionManager createTransactionManager() {
		return TransactionManagerServices.getTransactionManager();
	}
}
