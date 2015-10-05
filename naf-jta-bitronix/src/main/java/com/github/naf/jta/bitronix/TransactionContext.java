package com.github.naf.jta.bitronix;

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;

import bitronix.tm.TransactionManagerServices;

public class TransactionContext implements AlterableContext {
	private final static TransactionManager tm = TransactionManagerServices
			.getTransactionManager();

	@Override
	public boolean isActive() {

		try {
			return tm.getStatus() == Status.STATUS_ACTIVE;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return TransactionScoped.class;
	}

	@Override
	public <T> T get(Contextual<T> contextual,
			CreationalContext<T> creationalContext) {
		T r = contextual.create(creationalContext);
		System.err.println("R=" + r);
		return r;
	}

	@Override
	public <T> T get(Contextual<T> contextual) {
		return get(contextual, null);
	}

	@Override
	public void destroy(Contextual<?> contextual) {
		// TODO Auto-generated method stub

	}

}
