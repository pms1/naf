package test;

import java.lang.annotation.Annotation;

import javax.enterprise.event.Event;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import com.github.naf.spi.Extension;

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

	private final Iterable<Extension> extensions;

	Application(Weld weld, Iterable<Extension> extensions) {
		// StartMain.PARAMETERS = args;
		this.weld = weld;
		this.container = weld.initialize();
		this.extensions = extensions;

		container.instance().select(new TypeLiteral<Event<AfterBootEvent>>() {
		}).get().fire(new AfterBootEvent());
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

	public void join() {
		for (Extension e : extensions)
			e.join();
	}

}
