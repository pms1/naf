import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@IB
@Priority(0)
public class IBI {
	@AroundInvoke
	public Object logMethodEntry(InvocationContext ctx) throws Exception {
		System.err.println("LME BEGIN");
		try {
			String s;
			return ctx.proceed();
		} finally {
			System.err.println("LME DONE");
		}
	}
}