package com.github.naf.samples.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;

import com.google.common.collect.Iterables;

public class Ext implements javax.enterprise.inject.spi.Extension {
	public Ext() {
		System.err.println("EXT");
	}

	static {
		System.err.println("SEXT");
	}

	void processProducer(@Observes ProcessProducer<?, EntityManager> pp, final BeanManager bm) {

		System.err.println("OBS2 " + pp);
	}

	public void x(@Observes ProcessBeanAttributes a) {
		System.err.println("OBS3 " + a + " " + a.getAnnotated());
	}

	class PayByQualifier1 extends AnnotationLiteral<Priority>implements Priority {

		@Override
		public int value() {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	class PayByQualifier2 extends AnnotationLiteral<javax.interceptor.Interceptor>
			implements javax.interceptor.Interceptor {

	}

	AnnotatedType tiat;

	public <X> void processInjectionTarget(@Observes ProcessAnnotatedType<X> pat) {
		System.err.println("OBS " + pat + " " + pat.getAnnotatedType().getJavaClass());

		if (pat.getAnnotatedType().getJavaClass().equals(TI.class)) {
			System.err.println("OBSOBS1 " + pat);

			AnnotatedType<X> del = pat.getAnnotatedType();
			pat.setAnnotatedType(new AnnotatedType<X>() {

				@Override
				public Type getBaseType() {
					return del.getBaseType();
				}

				@Override
				public Set<Type> getTypeClosure() {
					return del.getTypeClosure();
				}

				@Override
				public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
					System.err.println("XXXXXX1 " + del.getAnnotation(annotationType));
					// if (annotationType.equals(Priority.class))
					// return (T) new PayByQualifier1();
					// if
					// (annotationType.equals(javax.interceptor.Interceptor.class))
					// return (T) new PayByQualifier2();
					return del.getAnnotation(annotationType);
				}

				@Override
				public Set<Annotation> getAnnotations() {
					System.err.println("XXXXXX2 " + del.getAnnotations());
					Set<Annotation> result = new HashSet<>(del.getAnnotations());
					// result.add(new PayByQualifier1());
					// result.add(new PayByQualifier2());
					return result;
				}

				@Override
				public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
					// System.err.println("XXXXXX3 " + annotationType + " " +
					// del.isAnnotationPresent(annotationType));
					// if (annotationType.equals(Priority.class))
					// return true;
					// if
					// (annotationType.equals(javax.interceptor.Interceptor.class))
					// return true;
					return del.isAnnotationPresent(annotationType);
				}

				@Override
				public Class<X> getJavaClass() {
					return del.getJavaClass();
				}

				@Override
				public Set<AnnotatedConstructor<X>> getConstructors() {
					return del.getConstructors();
				}

				@Override
				public Set<AnnotatedMethod<? super X>> getMethods() {
					return del.getMethods();
				}

				@Override
				public Set<AnnotatedField<? super X>> getFields() {
					return del.getFields();
				}
			});
			tiat = pat.getAnnotatedType();
			System.err.println("OBSOBS2 " + pat);
		}
		if (pat.getAnnotatedType().isAnnotationPresent(PersistenceContext.class))
			throw new Error("pat=" + pat);

		for (AnnotatedField<? super X> f : pat.getAnnotatedType().getFields()) {
			System.err.println("OBS2 " + f + " " + f.isAnnotationPresent(PersistenceContext.class));

		}

		// || pat.getAnnotatedType().isAnnotationPresent(MessageDriven.class)) {
		// modifyAnnotatedTypeMetaData(pat);
		// } else if
		// (pat.getAnnotatedType().isAnnotationPresent(Interceptor.class)) {
		// processInterceptorDependencies(pat);
		// }
	}

	@Priority(0)
	@javax.interceptor.Interceptor
	static class TI {
		@Inject
		TransactionManager tm;
	}

	public void x(@Observes AfterBeanDiscovery x, BeanManager bm) {
		System.err.println("TMTM " + bm.getBeans(TransactionManager.class));
		Bean<?> tm = Iterables.getOnlyElement(bm.getBeans(TransactionManager.class));

		InjectionPoint ip = new InjectionPoint() {

			@Override
			public Type getType() {
				return TransactionManager.class;
			}

			@Override
			public Set<Annotation> getQualifiers() {
				return Collections.emptySet();
			}

			@Override
			public Bean<?> getBean() {
				return tm;
			}

			@Override
			public Member getMember() {
				try {
					return TI.class.getDeclaredField("tm");
				} catch (NoSuchFieldException | SecurityException e) {
					throw new Error(e);
				}
			}

			@Override
			public Annotated getAnnotated() {
				return new AnnotatedField() {

					@Override
					public boolean isStatic() {
						return false;
					}

					@Override
					public AnnotatedType getDeclaringType() {
						return tiat;
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
						return null;
					}

					@Override
					public Set<Annotation> getAnnotations() {
						return Collections.emptySet();
					}

					@Override
					public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
						return false;
					}

					@Override
					public Field getJavaMember() {
						try {
							return TI.class.getDeclaredField("tm");
						} catch (NoSuchFieldException | SecurityException e) {
							throw new Error(e);
						}
					}

				};
			}

			@Override
			public boolean isDelegate() {
				return false;
			}

			@Override
			public boolean isTransient() {
				return false;
			}

		};

		x.addBean(new Interceptor<Object>() {

			@Override
			public Object create(CreationalContext<Object> creationalContext) {
				System.err.println("XXXXXXXXXXXXXXXXXX");

				System.err.println("XXXXXXXXXXXXXXXXXX " + bm.getInjectableReference(ip, creationalContext));

				System.err.println(
						"XXXXXXXXXXXXXXXXXX " + bm.getReference(tm, TransactionManager.class, creationalContext));
				return new TI();
			}

			@Override
			public void destroy(Object instance, CreationalContext<Object> creationalContext) {
				System.err.println("XXXXXXXXXXXXXXXXXX");

			}

			@Override
			public Set<Type> getTypes() {
				return new HashSet<>(Arrays.asList(TI.class));
			}

			@Override
			public Set<Annotation> getQualifiers() {
				return Collections.emptySet();
			}

			@Override
			public Class<? extends Annotation> getScope() {
				return ApplicationScoped.class;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public Set<Class<? extends Annotation>> getStereotypes() {
				return Collections.emptySet();
			}

			@Override
			public boolean isAlternative() {
				return false;
			}

			@Override
			public Class<?> getBeanClass() {
				return TI.class;
			}

			@Override
			public Set<InjectionPoint> getInjectionPoints() {
				return Collections.singleton(ip);
			}

			@Override
			public boolean isNullable() {
				throw new UnsupportedOperationException();
			}

			class PayByQualifier extends AnnotationLiteral<Transactional>implements Transactional {

				@Override
				public TxType value() {
					return TxType.REQUIRED;
				}

				@Override
				public Class[] rollbackOn() {
					return new Class[0];
				}

				@Override
				public Class[] dontRollbackOn() {
					return new Class[0];
				}

				@Override
				public boolean equals(Object other) {
					throw new Error();
				}

				@Override
				public int hashCode() {
					throw new Error();
				}
			}

			@Override
			public Set<Annotation> getInterceptorBindings() {
				return Collections.singleton(new PayByQualifier());
			}

			@Override
			public boolean intercepts(InterceptionType type) {
				return true;
			}

			@Override
			public Object intercept(InterceptionType type, Object instance, InvocationContext ctx) throws Exception {
				System.err.println("INTERCEPT PROG START " + instance + " " + ((TI) instance).tm);
				try {
					return ctx.proceed();
				} finally {
					System.err.println("INTERCEPT PROG DONE");
				}
			}

		});
	}

	public <X> void pit(@Observes ProcessInjectionTarget<X> pit, BeanManager bm) {

		final InjectionTarget<X> it = pit.getInjectionTarget();
		final AnnotatedType<X> at = pit.getAnnotatedType();

		// Here we wrap all available Injection Targets in a
		// custom wrapper that will add custom behavior to
		// inject() method
		InjectionTarget<X> wrapper = new InjectionTarget<X>() {

			@Override
			public X produce(CreationalContext<X> ctx) {
				return it.produce(ctx);
			}

			@Override
			public void dispose(X instance) {
				it.dispose(instance);
			}

			@Override
			public Set<InjectionPoint> getInjectionPoints() {
				return it.getInjectionPoints();
			}

			// The container calls inject() method when it's performing field
			// injection and calling bean initializer methods.
			// Our custom wrapper will also check for fields annotated with
			// @Property and resolve them by invoking the Property Resolver
			// method
			@Override
			public void inject(X instance, CreationalContext<X> ctx) {
				it.inject(instance, ctx);

				for (Field field : at.getJavaClass().getDeclaredFields()) {
					System.err.println("FIELD " + field);
				}
			}

			@Override
			public void postConstruct(X instance) {
				it.postConstruct(instance);
			}

			@Override
			public void preDestroy(X instance) {
				it.preDestroy(instance);
			}

		};

		pit.setInjectionTarget(wrapper);
	}
	// private <X> void modifyAnnotatedTypeMetaData(ProcessAnnotatedType<X> pat)
	// {
	// Transactional transactionalAnnotation =
	// AnnotationInstanceProvider.of(Transactional.class);
	// RequestScoped requestScopedAnnotation =
	// AnnotationInstanceProvider.of(RequestScoped.class);
	// AnnotatedType at = pat.getAnnotatedType();
	//
	// AnnotatedTypeBuilder<X> builder = new
	// AnnotatedTypeBuilder<X>().readFromType(at);
	// builder.addToClass(transactionalAnnotation).addToClass(requestScopedAnnotation);
	// addInjectAnnotationToFields(at, builder);
	// // Set the wrapper instead the actual annotated type
	// pat.setAnnotatedType(builder.create());
	// }
	//
	// private <X> boolean
	// shouldInjectionAnnotationBeAddedToField(AnnotatedField<? super X> field)
	// {
	// return !field.isAnnotationPresent(Inject.class) &&
	// (field.isAnnotationPresent(Resource.class)
	// || field.isAnnotationPresent(EJB.class) ||
	// field.isAnnotationPresent(PersistenceContext.class));
	// }
	//
	// private <X> void addInjectAnnotationToFields(final AnnotatedType<X>
	// annotatedType,
	// AnnotatedTypeBuilder<X> builder) {
	// Inject injectAnnotation = AnnotationInstanceProvider.of(Inject.class);
	// for (AnnotatedField<? super X> field : annotatedType.getFields()) {
	// if (shouldInjectionAnnotationBeAddedToField(field)) {
	// builder.addToField(field, injectAnnotation);
	// }
	// }
	// }
}
