package com.github.naf.jta.bitronix;

import java.lang.annotation.Annotation;

import org.jboss.weld.context.AbstractContext;
import org.jboss.weld.context.beanstore.BeanStore;

public class TC2 extends AbstractContext {

	public TC2(String contextId, boolean multithreaded) {
		super(contextId, multithreaded);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Class<? extends Annotation> getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected BeanStore getBeanStore() {
		// TODO Auto-generated method stub
		return null;
	}

}
