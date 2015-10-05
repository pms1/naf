package com.github.naf.jta;

import javax.annotation.Priority;
import javax.interceptor.Interceptor;

@Priority(0)
@Interceptor
class TransactionalInterceptor {
	final TransactionalHandler handler;

	TransactionalInterceptor(TransactionalHandler handler) {
		this.handler = handler;
	}

	@Override
	public String toString() {
		return super.toString() + " with handler = " + handler;
	}
}