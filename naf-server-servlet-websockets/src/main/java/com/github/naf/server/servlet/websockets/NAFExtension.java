package com.github.naf.server.servlet.websockets;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import com.github.naf.server.servlet.ServletInitializationEvent;
import com.github.naf.spi.Extension;
import com.google.common.collect.ImmutableSet;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;

public class NAFExtension implements Extension {

	void init(@Observes ServletInitializationEvent event, BeanManager bm) throws ServletException {

		// Initialize javax.websocket layer
		org.eclipse.jetty.websocket.jsr356.server.ServerContainer wscontainer = WebSocketServerContainerInitializer
				.configureContext(event.getContext());

		endPoints.values().forEach(c -> {
			System.err.println("C=" + c);
			try {
				wscontainer.addEndpoint(c);
			} catch (Exception e) {
				throw new Error(e);
			}
		});
	}

	static boolean delegateMethod(Method m) {
		return m.isAnnotationPresent(OnOpen.class) || m.isAnnotationPresent(OnClose.class)
				|| m.isAnnotationPresent(OnError.class) || m.isAnnotationPresent(OnMessage.class);
	}

	static final ClassPool cp = new ClassPool(ClassPool.getDefault());
	static final Helper cpx = new Helper(cp);

	Class<?> doit(Class<?> c) {
		try {
			CtClass ctClass = cp.makeClass(c.getName() + "$Proxy", cp.get(Object.class.getName()));
			javassist.bytecode.ClassFile ccFile = ctClass.getClassFile();
			ConstPool constpool = ccFile.getConstPool();

			try {
				System.err.println("AAA " + cpx.get(c).getAnnotation(ServerEndpoint.class));
				System.err.println("AAA " + cpx.get(c).getAnnotation(ServerEndpoint.class).getClass());
				System.err.println("AAA " + cpx.get(c).getClassFile().getAttribute(AnnotationsAttribute.visibleTag));
				System.err.println(
						"AAA " + cpx.get(c).getClassFile().getAttribute(AnnotationsAttribute.visibleTag).getClass());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			cpx.copyAnnotation(ctClass, c, ServerEndpoint.class);
			// cpx.addAnnotation(ctClass,
			// c.getAnnotation(ServerEndpoint.class));

			AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
			javassist.bytecode.annotation.Annotation annot = new javassist.bytecode.annotation.Annotation(
					Inject.class.getName(), constpool);
			attr.addAnnotation(annot);

			CtField f = new CtField(cp.get(c.getName()), "delegate", ctClass);
			f.getFieldInfo().addAttribute(attr);
			ctClass.addField(f);

			f = new CtField(cp.get(CustomScopeExtension.class.getName()), "cse", ctClass);
			f.getFieldInfo().addAttribute(attr);
			ctClass.addField(f);

			for (Class<?> c1 = c; !c1.equals(Object.class); c1 = c1.getSuperclass()) {
				for (Method m : c1.getDeclaredMethods()) {
					String cseMethod = null;
					Class<? extends Annotation> a = null;
					if (m.isAnnotationPresent(OnOpen.class)) {
						cseMethod = "onOpen";
						a = OnOpen.class;
					}
					if (m.isAnnotationPresent(OnClose.class)) {
						cseMethod = "onClose";
						a = OnClose.class;
					}
					if (m.isAnnotationPresent(OnMessage.class)) {
						cseMethod = "onMessage";
						a = OnMessage.class;
					}
					if (m.isAnnotationPresent(OnError.class)) {
						cseMethod = "onError";
						a = OnError.class;
					}
					if (cseMethod == null)
						continue;
					if (!m.getReturnType().equals(Void.TYPE))
						throw new Error();

					Class<?>[] oldParams = m.getParameterTypes();
					List<CtClass> newParams = new ArrayList<>(oldParams.length + 1);
					int sessionParameter = -1;
					String paramList = "";
					for (int i = 0; i != oldParams.length; ++i) {
						newParams.add(cpx.get(oldParams[i]));
						if (oldParams[i].equals(Session.class))
							sessionParameter = i + 1;
						if (!paramList.isEmpty())
							paramList += ",";
						paramList += "$" + (i + 1);

						// FIXME: copy parameter annotations
					}
					if (sessionParameter == -1) {
						sessionParameter = oldParams.length + 1;
						newParams.add(cpx.get(Session.class));
					}

					CtMethod m1 = new CtMethod(CtClass.voidType, m.getName(), newParams.toArray(new CtClass[0]),
							ctClass);
					String body;
					body = "{";
					body += SessionBinding.class.getName() + " $sessionBinding = cse." + cseMethod + "($"
							+ sessionParameter + ");";
					body += "try { delegate." + m.getName() + "(" + paramList + "); }";
					body += "finally { $sessionBinding.close(); }";
					body += "}";
					System.err.println(body);
					m1.setBody(body);

					cpx.copyAnnotation(m1, m, a);

					ctClass.addMethod(m1);

				}
			}

			ctClass.writeFile("c:/temp");
			Class<?> result = ctClass.toClass(c.getClassLoader(), c.getProtectionDomain());
			System.err.println("ZZZ " + result.getAnnotation(ServerEndpoint.class));
			return result;
		} catch (NotFoundException | CannotCompileException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	Map<Class<?>, Class<?>> endPoints = new HashMap<>();

	<X> void process(@Observes ProcessAnnotatedType<X> pat) {

		if (pat.getAnnotatedType().isAnnotationPresent(ServerEndpoint.class)) {
			Class<?> c = pat.getAnnotatedType().getJavaClass();
			System.err.println("PATPAT " + c);
			if (endPoints.values().contains(c))
				return;

			endPoints.put(c, doit(c));
		}
	}

	static class AnnotatedImpl implements Annotated {
		final AnnotatedElement c;
		final Type t;

		AnnotatedImpl(Type t, AnnotatedElement c) {
			Objects.requireNonNull(t);
			Objects.requireNonNull(c);
			this.t = t;
			this.c = c;
		}

		@Override
		public Type getBaseType() {
			return t;
		}

		@Override
		public Set<Type> getTypeClosure() {
			return ImmutableSet.of(t, Object.class);
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
			return c.getAnnotation(annotationType);
		}

		@Override
		public Set<Annotation> getAnnotations() {
			return ImmutableSet.copyOf(c.getAnnotations());
		}

		@Override
		public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
			return c.isAnnotationPresent(annotationType);
		}

		@Override
		public <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
			return ImmutableSet.copyOf(c.getAnnotationsByType(annotationType));
		}

	}

	static class AnnotatedParameterImpl<X> extends AnnotatedImpl implements AnnotatedParameter<X> {
		private final int position;
		private final AnnotatedCallable<X> declaringCallable;

		AnnotatedParameterImpl(AnnotatedCallable<X> declaringCallable, int position, Parameter p) {
			super(p.getParameterizedType(), p);
			Objects.requireNonNull(declaringCallable);
			this.declaringCallable = declaringCallable;
			if (position < 0)
				throw new IllegalArgumentException();
			this.position = position;
		}

		@Override
		public int getPosition() {
			return position;
		}

		@Override
		public AnnotatedCallable<X> getDeclaringCallable() {
			return declaringCallable;
		}
	}

	static <X> List<AnnotatedParameter<X>> as(AnnotatedCallable<X> declaringCallable, Parameter[] p) {
		List<AnnotatedParameter<X>> result = new ArrayList<>(p.length);
		for (int i = 0; i != p.length; ++i)
			result.add(new AnnotatedParameterImpl<X>(declaringCallable, i, p[i]));
		return result;
	}

	static <X> AnnotatedConstructor<X> as(AnnotatedType<X> type, Constructor<X> c) {
		return new AnnotatedConstructorImpl<X>(type, c);
	}

	static <X> AnnotatedField<X> as(AnnotatedType<X> type, Field f) {
		return new AnnotatedField<X>() {

			@Override
			public boolean isStatic() {
				return Modifier.isStatic(f.getModifiers());
			}

			@Override
			public AnnotatedType<X> getDeclaringType() {
				return type;
			}

			@Override
			public Type getBaseType() {
				throw new Error();
			}

			@Override
			public Set<Type> getTypeClosure() {
				throw new Error();
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
				return f.getAnnotation(annotationType);
			}

			@Override
			public Set<Annotation> getAnnotations() {
				return ImmutableSet.copyOf(f.getAnnotations());
			}

			@Override
			public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
				return f.isAnnotationPresent(annotationType);
			}

			@Override
			public Field getJavaMember() {
				return f;
			}
		};
	}

	static class AnnotatedMethodImpl<X> extends AnnotatedImpl implements AnnotatedMethod<X> {
		private final Method method;
		private final AnnotatedType<X> declaringType;

		AnnotatedMethodImpl(AnnotatedType<X> declaringType, Method method) {
			super(method.getGenericReturnType(), method);
			this.declaringType = declaringType;
			this.method = method;
		}

		@Override
		public List<AnnotatedParameter<X>> getParameters() {
			return as(this, method.getParameters());
		}

		@Override
		public boolean isStatic() {
			return Modifier.isStatic(method.getModifiers());
		}

		@Override
		public AnnotatedType<X> getDeclaringType() {
			return declaringType;
		}

		@Override
		public Method getJavaMember() {
			return method;
		}

	}

	static class AnnotatedConstructorImpl<X> extends AnnotatedImpl implements AnnotatedConstructor<X> {
		private final Constructor<X> constructor;
		private final AnnotatedType<X> declaringType;

		AnnotatedConstructorImpl(AnnotatedType<X> declaringType, Constructor<X> constructor) {
			super(constructor.getDeclaringClass(), constructor);
			this.declaringType = declaringType;
			this.constructor = constructor;
		}

		@Override
		public List<AnnotatedParameter<X>> getParameters() {
			return as(this, constructor.getParameters());
		}

		@Override
		public boolean isStatic() {
			return Modifier.isStatic(constructor.getModifiers());
		}

		@Override
		public AnnotatedType<X> getDeclaringType() {
			return declaringType;
		}

		@Override
		public Constructor<X> getJavaMember() {
			return constructor;
		}

	}

	static <X> AnnotatedMethod<X> as(AnnotatedType<X> type, Method m) {
		return new AnnotatedMethodImpl<X>(type, m);
	}

	static <X> AnnotatedType<X> as(Class<X> c) {
		return new AnnotatedType<X>() {

			@Override
			public Type getBaseType() {
				return c;
			}

			@Override
			public Set<Type> getTypeClosure() {
				return ImmutableSet.of(c, Object.class);
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
				return c.getAnnotation(annotationType);
			}

			@Override
			public Set<Annotation> getAnnotations() {
				return ImmutableSet.copyOf(c.getAnnotations());
			}

			@Override
			public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
				return c.isAnnotationPresent(annotationType);
			}

			@Override
			public Class<X> getJavaClass() {
				return c;
			}

			@Override
			public Set<AnnotatedConstructor<X>> getConstructors() {
				return Arrays.stream((Constructor<X>[]) c.getConstructors()).map(c1 -> as(this, c1))
						.collect(Collectors.toSet());
			}

			@Override
			public Set<AnnotatedMethod<? super X>> getMethods() {
				return Arrays.stream(c.getMethods()).map(m -> as(this, m)).collect(Collectors.toSet());
			}

			@Override
			public Set<AnnotatedField<? super X>> getFields() {
				return Arrays.stream(c.getFields()).map(f -> as(this, f)).collect(Collectors.toSet());
			}

		};
	}

	void process(@Observes AfterTypeDiscovery afterTypeDiscovery) {
		for (Class<?> c : endPoints.values())
			afterTypeDiscovery.addAnnotatedType(as(c), null);
	}
}
