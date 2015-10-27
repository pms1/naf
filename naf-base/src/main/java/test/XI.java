package test;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.ejb.SessionBeanInterceptor;

@Priority(0)
@Interceptor
public class XI extends SessionBeanInterceptor {
	public XI() {

		System.err.println("AIAIAI " + this);
	}

	static {
		System.err.println("AIAIAI ");
	}

	@AroundInvoke
	public Object aroundInvoke(InvocationContext invocation) throws Exception {
		System.err.println("AIAIAI " + invocation);
		return super.aroundInvoke(invocation);
	}
}
