package pkg;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.manager.BeanManagerImpl;

import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;

public class JaxrsServletSampleMain {
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		try (Application a = new ApplicationBuilder().build()) {
			BeanManagerImpl bm = ((BeanManagerProxy) a.get(BeanManager.class)).delegate();

			// Map<String, Object> requestMap = new HashMap<String, Object>();
			// activateContext(beanManager, RequestScoped.class, requestMap);
			//
			// activateContext(beanManager, ConversationScoped.class,
			// new MutableBoundRequest(requestMap, sessionMap));
			// }
			//
			// /**
			// * Activates a context for a given manager.
			// * @param beanManager in which the context is activated
			// * @param cls the class that represents the scope
			// * @param storage in which to put the scoped values
			// * @param <S> the type of the storage
			// */
			// private <S> void activateContext(final BeanManager beanManager,
			// final Class<? extends Annotation> cls, final S storage) {

			Method m = BeanManagerImpl.class.getDeclaredMethod("getContexts");
			m.setAccessible(true);
			System.err.println(Arrays.toString(m.getParameters()));
			AbstractBoundContext<Object> context;
			Map<Class<? extends Annotation>, List<Context>> m1 = (Map) m.invoke(bm);
			System.err.println("m1 " + m1);
			System.err.println("m1 " + m1.keySet());
			context = (AbstractBoundContext<Object>) m1.get(javax.enterprise.context.RequestScoped.class).get(0);
			context.associate(new HashMap());
			context.activate();
			a.get(T1.class).doIt();
			context.deactivate();

			a.join();
		}
	}
}
