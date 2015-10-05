package com.github.naf.samples.jta;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.transaction.UserTransaction;

@Dependent
public class JtaHelloService {
	@Inject
	UserTransaction ut;

	@Produces
	String foobar;

	@Transactional
	public void hello() {
		System.err.println("hello, world\nut=" + ut);
	}

	@Transactional(TxType.NEVER)
	public void hello2() {
		System.err.println("hello2, world\nut=" + ut);
	}
}
