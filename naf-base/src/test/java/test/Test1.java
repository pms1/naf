package test;
import javax.annotation.Resource;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.bean.ContextualInstance;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.manager.BeanManagerImpl;
import org.junit.Test;

import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;

public class Test1 {
	static class P1 {

		public void doit() {
			System.err.println("doit");

			ts.createTimer(1000, "foo");
		}

		@Resource
		TimerService ts;

		@Timeout
		public void xx() {
			System.err.println("timeout");
		}
	}

	@Test
	public void t1() {
		try (Application a = new ApplicationBuilder().build()) {
			B1 b1 = a.get(B1.class);
			System.err.println("b1=" + b1);
			b1 = a.get(B1.class);
			System.err.println("b1=" + b1);
			// b1.b1();
			// b1.b2();
			a.get(BeanManager.class).fireEvent(new String("foo"));

			if (true) {
				BeanManager bm = a.get(BeanManager.class);
				if (bm instanceof BeanManagerProxy)
					bm = ((BeanManagerProxy) bm).delegate();
				Bean<?> next = bm.getBeans(B1.class).iterator().next();
				System.err.println("N=" + next);
				Object ifExists = ContextualInstance.getIfExists(next, (BeanManagerImpl) bm);
				System.err.println("XXX " + ifExists);

				CreationalContext ctx = new CreationalContext<Object>() {

					@Override
					public void push(Object incompleteInstance) {
						// TODO Auto-generated method stub

					}

					@Override
					public void release() {
						// TODO Auto-generated method stub

					}
				};
				ifExists = ContextualInstance.get(next, (BeanManagerImpl) bm, ctx);
				System.err.println("XXX " + ifExists);
				System.err.println("XXX " + ifExists.getClass());
			}

			a.get(P1.class).doit();
		}
	}
}
