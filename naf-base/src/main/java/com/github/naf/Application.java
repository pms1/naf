package com.github.naf;

import java.lang.annotation.Annotation;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import com.github.naf.spi.AfterBootEvent;
import com.github.naf.spi.Extension;
import com.github.naf.spi.ShutdownEvent;

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

	Application(Weld weld, WeldContainer container, Iterable<Extension> extensions) {
		// StartMain.PARAMETERS = args;
		this.weld = weld;
		this.container = container;
		this.extensions = extensions;

		try {
			container.instance().select(new TypeLiteral<Event<AfterBootEvent>>() {
			}).get().fire(new AfterBootEvent());
		} catch (ObserverException e) {
			try {
				container.instance().select(new TypeLiteral<Event<ShutdownEvent>>() {
				}).get().fire(new ShutdownEvent());
			} finally {
				weld.shutdown();
			}

			throw new Error(e);
		}
	}

	@Override
	public void close() {
		if (container.isRunning()) {
			container.instance().select(new TypeLiteral<Event<ShutdownEvent>>() {
			}).get().fire(new ShutdownEvent());

			weld.shutdown();
		}
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
