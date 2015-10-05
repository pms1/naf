package com.github.naf.jta.bitronix;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.weld.transaction.spi.TransactionServices;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

public class BitronixTransactionServices implements TransactionServices {

	private BitronixTransactionManager utm;

	public BitronixTransactionServices() {
		TransactionManagerServices.getConfiguration().setDefaultTransactionTimeout(10 * 60);
		TransactionManagerServices.getConfiguration().setGracefulShutdownInterval(0);
		TransactionManagerServices.getConfiguration().setJournal("null");
		TransactionManagerServices.getConfiguration()
				.setServerId(java.lang.management.ManagementFactory.getRuntimeMXBean().getName());

		utm = TransactionManagerServices.getTransactionManager();
	}

	@Override
	public void cleanup() {
		utm.shutdown();
	}

	@Override
	public void registerSynchronization(Synchronization synchronizedObserver) {
		try {
			utm.getTransaction().registerSynchronization(synchronizedObserver);
		} catch (IllegalStateException | RollbackException | SystemException e) {
			throw new Error(e);
		}
	}

	@Override
	public boolean isTransactionActive() {
		try {
			return utm.getTransaction() != null && utm.getTransaction().getStatus() == Status.STATUS_ACTIVE;
		} catch (SystemException e) {
			throw new Error(e);
		}
	}

	@Override
	public UserTransaction getUserTransaction() {
		return utm;
	}

}