package com.github.naf.jta.bitronix;

import java.lang.annotation.Annotation;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;

import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.contexts.AbstractContext;
import org.jboss.weld.contexts.beanstore.BeanStore;
import org.jboss.weld.contexts.beanstore.HashMapBeanStore;

import bitronix.tm.TransactionManagerServices;

public class TransactionContext extends AbstractContext {

	private final static TransactionManager tm = TransactionManagerServices.getTransactionManager();

	public TransactionContext() {
		super(RegistrySingletonProvider.STATIC_INSTANCE, false);
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return TransactionScoped.class;
	}

	@Override
	protected BeanStore getBeanStore() {
		BeanStore beanStore = (BeanStore) TransactionManagerServices.getTransactionSynchronizationRegistry()
				.getResource(this);
		if (beanStore == null) {
			beanStore = new HashMapBeanStore();
			TransactionManagerServices.getTransactionSynchronizationRegistry().putResource(this, beanStore);
			try {
				tm.getTransaction().registerSynchronization(sync);
			} catch (IllegalStateException | RollbackException | SystemException e) {
				throw new Error(e);
			}
		}
		return beanStore;
	}

	@Override
	public boolean isActive() {
		try {
			switch (tm.getStatus()) {
			case Status.STATUS_ACTIVE:
			case Status.STATUS_MARKED_ROLLBACK:
			case Status.STATUS_PREPARED:
			case Status.STATUS_PREPARING:
			case Status.STATUS_COMMITTING:
			case Status.STATUS_ROLLING_BACK:
				return true;
			case Status.STATUS_COMMITTED:
			case Status.STATUS_NO_TRANSACTION:
			case Status.STATUS_ROLLEDBACK:
				return false;
			case Status.STATUS_UNKNOWN:
			default:
				throw new Error("unhandled transaction status=" + tm.getStatus());
			}
		} catch (SystemException e) {
			throw new Error("unhandled transaction status", e);
		}
	}

	private Synchronization sync = new Synchronization() {

		@Override
		public void beforeCompletion() {
		}

		@Override
		public void afterCompletion(int status) {
			destroy();

			TransactionManagerServices.getTransactionSynchronizationRegistry().putResource(TransactionContext.this,
					null);
		}
	};
}
