package com.github.naf.server.servlet.websockets;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.websocket.Session;

import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.contexts.AbstractBoundContext;
import org.jboss.weld.contexts.beanstore.AttributeBeanStore;
import org.jboss.weld.contexts.beanstore.LockStore;
import org.jboss.weld.contexts.beanstore.NamingScheme;
import org.jboss.weld.contexts.beanstore.SimpleNamingScheme;

public class Context2 extends AbstractBoundContext<Session> {

	public Context2() {
		super(RegistrySingletonProvider.STATIC_INSTANCE, true);
	}

	static final NamingScheme namingScheme = new SimpleNamingScheme(Context2.class.getName());

	static class SessionBeanStore extends AttributeBeanStore {

		private Session session;

		public SessionBeanStore(Session session) {
			super(namingScheme, false);
			Objects.requireNonNull(session);
			this.session = session;
		}

		@Override
		protected Object getAttribute(String prefixedId) {
			return session.getUserProperties().get(prefixedId);
		}

		@Override
		protected void removeAttribute(String prefixedId) {
			session.getUserProperties().remove(prefixedId);
		}

		@Override
		protected Iterator<String> getAttributeNames() {
			return new HashSet<>(session.getUserProperties().keySet()).iterator();
		}

		@Override
		protected void setAttribute(String prefixedId, Object instance) {
			session.getUserProperties().put(prefixedId, instance);
		}

		@Override
		protected LockStore getLockStore() {
			return null;
		}

	}

	@Override
	public boolean associate(Session storage) {
		if (getBeanStore() == null) {
			setBeanStore(new SessionBeanStore(storage));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return RequestScoped.class;
	}

	public void end(Session session) {
		associate(session);
		activate();
		invalidate();
		deactivate();
		dissociate(session);
	}

	static String asString(Object o) {
		if (o == null)
			return String.valueOf(o);
		else
			return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
	}
}
