package com.github.naf.jta;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;

abstract class AbstractTransactionInterceptor {
	@Inject
	private TransactionManager tm;

	// @Inject
	private Logger logger;

	private Transactional getTransactionAnnotation(InvocationContext ic) {
		Transactional result = ic.getMethod().getAnnotation(Transactional.class);
		if (result != null)
			return result;
		result = ic.getMethod().getDeclaringClass().getAnnotation(Transactional.class);
		if (result != null)
			return result;
		throw new Error("no @Transactional found for " + ic);
	}

	@AroundInvoke
	public Object aroundInvoke(InvocationContext ic) throws Throwable {
		boolean remove = false;

		logger.log(Level.FINE, "UTM " + this + " " + ic.getMethod());

		Transaction suspended = null;

		Transactional t = getTransactionAnnotation(ic);
		switch (t.value()) {
		case REQUIRED:
			if (tm.getTransaction() == null) {
				remove = true;
				logger.log(Level.FINE,
						"UTM " + this + " " + ic.getMethod() + " BEGIN REQUIRED BEFORE " + tm.getTransaction());
				tm.begin();
				logger.log(Level.FINE,
						"UTM " + this + " " + ic.getMethod() + " BEGIN REQUIRED AFTER " + tm.getTransaction());
			} else {
				logger.log(Level.FINE,
						"UTM " + this + " " + ic.getMethod() + " BEGIN REQUIRED BEFORE REUSE " + tm.getStatus());
				if (tm.getStatus() != Status.STATUS_ACTIVE)
					throw new Error("current transaction status is " + statusToString(tm.getStatus()) + ", expected "
							+ statusToString(Status.STATUS_ACTIVE));
			}
			break;
		case REQUIRES_NEW:
			remove = true;
			logger.log(Level.FINE,
					"UTM " + this + " " + ic.getMethod() + " BEGIN REQUIRES_NEW BEFORE " + tm.getTransaction());
			suspended = tm.suspend();
			logger.log(Level.FINE,
					"UTM " + this + " " + ic.getMethod() + " BEGIN REQUIRES_NEW SUSPEND " + tm.getTransaction());
			tm.begin();
			logger.log(Level.FINE,
					"UTM " + this + " " + ic.getMethod() + " BEGIN REQUIRES_NEW AFTER " + tm.getTransaction());
			break;
		default:
			throw new UnsupportedOperationException("transaction type " + t.value());
		}

		Object resultReturned = null;
		Throwable resultThrown = null;
		try {
			resultReturned = ic.proceed();
		} catch (Throwable t1) {
			resultThrown = t1;
		}
		logger.log(Level.FINE, "RESULT " + (resultReturned != null) + " " + resultThrown);

		if (remove) {
			if (resultThrown == null) {
				logger.log(Level.FINE, "UTM " + this + " " + ic.getMethod() + " COMMIT BEFORE " + tm.getTransaction());
				tm.commit();
				logger.log(Level.FINE, "UTM " + this + " " + ic.getMethod() + " COMMIT AFTER " + tm.getTransaction());
			} else {
				logger.log(Level.FINE,
						"UTM " + this + " " + ic.getMethod() + " ROLLBACK BEFORE " + tm.getTransaction());
				tm.rollback();
				logger.log(Level.FINE, "UTM " + this + " " + ic.getMethod() + " ROLLBACK AFTER " + tm.getTransaction());
			}
		}

		if (suspended != null) {
			logger.log(Level.FINE, "UTM " + this + " " + ic.getMethod() + " RESUME BEFORE " + tm.getTransaction());
			tm.resume(suspended);
			logger.log(Level.FINE, "UTM " + this + " " + ic.getMethod() + " RESUME AFTER " + tm.getTransaction());
		}

		if (resultThrown != null)
			throw resultThrown;
		return resultReturned;
	}

	private static String statusToString(int status) {
		switch (status) {
		case Status.STATUS_ACTIVE:
			return "STATUS_ACTIVE";
		case Status.STATUS_COMMITTED:
			return "STATUS_COMMITTED";
		case Status.STATUS_COMMITTING:
			return "STATUS_COMMITTING";
		case Status.STATUS_MARKED_ROLLBACK:
			return "STATUS_MARKED_ROLLBACK";
		case Status.STATUS_NO_TRANSACTION:
			return "STATUS_NO_TRANSACTION";
		case Status.STATUS_PREPARED:
			return "STATUS_PREPARED";
		case Status.STATUS_PREPARING:
			return "STATUS_PREPARING";
		case Status.STATUS_ROLLEDBACK:
			return "STATUS_ROLLEDBACK";
		case Status.STATUS_ROLLING_BACK:
			return "STATUS_ROLLING_BACK";
		case Status.STATUS_UNKNOWN:
			return "STATUS_UNKNOWN";
		default:
			return "STATUS_UNHANDLED(" + status + ")";
		}
	}

}
