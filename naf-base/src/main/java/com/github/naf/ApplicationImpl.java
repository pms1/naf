package com.github.naf;

import java.lang.annotation.Annotation;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.Unbound;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import com.github.naf.spi.AfterBootEvent;
import com.github.naf.spi.ApplicationContext;
import com.github.naf.spi.Extension;
import com.github.naf.spi.ShutdownEvent;

class ApplicationImpl implements Application {

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
	private final ApplicationContext ac;
	private final Runnable shutdownHook;

	ApplicationImpl(ApplicationContext ac, Weld weld, WeldContainer container, Iterable<Extension> extensions,
			Runnable shutdownHook) {
		this.weld = weld;
		this.container = container;
		this.extensions = extensions;
		this.ac = ac;
		this.shutdownHook = shutdownHook;

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
		shutdownHook.run();
		ApplicationBuilder.Holder.namingManager.setApplicationContext(null);
	}

	@Override
	public <T> T get(Class<T> clazz, Annotation... annotations) {
		return container.instance().select(clazz, annotations).get();
	}

	@Override
	public <T> Iterable<T> getAll(Class<T> clazz, Annotation... annotations) {
		return container.instance().select(clazz, annotations);
	}

	@Override
	public void join() {
		for (Extension e : extensions)
			e.join();
	}

	@Override
	public void withRequestContext(Runnable object) {
		RequestContext rc = container.instance().select(RequestContext.class, new AnnotationLiteral<Unbound>() {
		}).get();

		rc.activate();
		try {
			object.run();
		} finally {
			rc.deactivate();
		}
	}

}
