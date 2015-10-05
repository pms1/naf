package com.github.naf.jta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.interceptor.InvocationContext;
import javax.transaction.Transactional.TxType;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

class CDIExtension implements javax.enterprise.inject.spi.Extension {

	private final class TransactionalInterceptorBean implements Interceptor<TransactionalInterceptor> {
		final BeanManager bm;
		final Bean<?> transactionHandler;
		final TxType type;

		TransactionalInterceptorBean(BeanManager bm, Bean<?> transactionHandler, TxType type) {
			Objects.requireNonNull(bm);
			this.bm = bm;
			Objects.requireNonNull(transactionHandler);
			this.transactionHandler = transactionHandler;
			Objects.requireNonNull(type);
			this.type = type;
		}

		@Override
		public TransactionalInterceptor create(CreationalContext<TransactionalInterceptor> creationalContext) {
			return new TransactionalInterceptor((TransactionalHandler) bm.getReference(transactionHandler,
					TransactionalHandler.class, creationalContext));
		}

		@Override
		public void destroy(TransactionalInterceptor instance,
				CreationalContext<TransactionalInterceptor> creationalContext) {
		}

		@Override
		public Set<Type> getTypes() {
			return Collections.singleton(TransactionalInterceptor.class);
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
			return TransactionalInterceptor.class;
		}

		@Override
		public Set<InjectionPoint> getInjectionPoints() {
			return Collections.emptySet();
		}

		@Override
		public boolean isNullable() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<Annotation> getInterceptorBindings() {
			return Sets.newHashSet(new TransactionalQualifier(type));
		}

		@Override
		public boolean intercepts(InterceptionType type) {
			return type == InterceptionType.AROUND_INVOKE;
		}

		@Override
		public Object intercept(InterceptionType type, TransactionalInterceptor instance, InvocationContext ctx)
				throws Exception {
			return instance.handler.handle(ctx);
		}
	}

	// if this method is removed, the interceptor no longer works. I have no
	// idea why...
	public void processInterceptorType(@Observes ProcessAnnotatedType<TransactionalInterceptor> pat) {
	}

	public void addTransactionalInterceptors(@Observes AfterBeanDiscovery x, BeanManager bm) {
		Bean<?> ti = Iterables.getOnlyElement(bm.getBeans(TransactionalHandler.class));

		for (TxType type : TxType.values())
			x.addBean(new TransactionalInterceptorBean(bm, ti, type));
	}

}
