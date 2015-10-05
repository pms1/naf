package com.github.naf.jta;

import javax.enterprise.util.AnnotationLiteral;
import javax.transaction.Transactional;

class TransactionalQualifier extends AnnotationLiteral<Transactional>implements Transactional {
	private static final long serialVersionUID = 7318394598066014491L;

	final TxType type;

	public TransactionalQualifier(TxType type) {
		this.type = type;
	}

	@Override
	public TxType value() {
		return type;
	}

	private static final Class<?>[] EMPTY_ARRAY = new Class[0];

	@Override
	public Class<?>[] rollbackOn() {
		return EMPTY_ARRAY;
	}

	@Override
	public Class<?>[] dontRollbackOn() {
		return EMPTY_ARRAY;
	}
}