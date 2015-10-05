package test;

import java.lang.annotation.Annotation;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class Application implements AutoCloseable {

	private final Weld weld;
	private final WeldContainer container;

	static {
		// StackTraceUtils.addClassTest(new Closure<Boolean>(null) {
		// public Boolean doCall(String args) {
		// if (args.startsWith("sun.reflect."))
		// return false;
		// if (args.equals("java.lang.reflect.Method"))
		// return false;
		// if (args.startsWith("org.jboss.weld.interceptor."))
		// return false;
		// if (args.contains("$Proxy$_$$_Weld"))
		// return false;
		// return null;
		// }
		// });
	}

	public Application(Weld weld) {
		// StartMain.PARAMETERS = args;
		this.weld = weld;
		this.container = weld.initialize();
	}

	@Override
	public void close() {
		if (container.isRunning())
			weld.shutdown();
	}

	public <T> T get(Class<T> clazz, Annotation... annotations) {
		return container.instance().select(clazz, annotations).get();
	}

	public <T> Iterable<T> getAll(Class<T> clazz, Annotation... annotations) {
		return container.instance().select(clazz, annotations);
	}

	public void shutdown() {
		// TODO Auto-generated method stub

	}

}