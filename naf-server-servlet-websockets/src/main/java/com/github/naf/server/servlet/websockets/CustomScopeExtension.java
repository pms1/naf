package com.github.naf.server.servlet.websockets;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.websocket.Session;

/**
 *
 * @author rmpestano
 */
public class CustomScopeExtension implements Extension {

	private Context2 context;

	public void registerContext(@Observes final AfterBeanDiscovery event) {
		if (context != null)
			throw new Error();
		context = new Context2();
		event.addContext(context);
	}

	public SessionBinding onMessage(Session session) {
		System.err.println("CSE onMessage");
		context.associate(session);
		context.activate();
		return new SessionBindingImpl(session);
	}

	public SessionBinding onError(Session session) {
		System.err.println("CSE onError");
		context.associate(session);
		context.activate();
		return new SessionBindingImpl(session);

	}

	public SessionBinding onClose(Session session) {
		System.err.println("CSE onClose");
		context.associate(session);
		context.activate();
		context.invalidate();
		return new SessionBindingImpl(session);
	}

	public SessionBinding onOpen(Session session) {
		System.err.println("CSE onOpen");
		context.associate(session);
		context.activate();
		return new SessionBindingImpl(session);
	}

	class SessionBindingImpl implements SessionBinding {

		private final Session session;

		public SessionBindingImpl(Session session) {
			this.session = session;
		}

		@Override
		public void close() {
			System.err.println("CSE close");
			if (context.isActive())
				context.deactivate();
			context.dissociate(session);
		}

	}
}