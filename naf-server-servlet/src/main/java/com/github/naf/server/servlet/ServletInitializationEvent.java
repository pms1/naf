package com.github.naf.server.servlet;

import java.util.Objects;

import org.eclipse.jetty.servlet.ServletContextHandler;

public class ServletInitializationEvent {
	private final ServletContextHandler context;

	public ServletInitializationEvent(ServletContextHandler context) {
		Objects.requireNonNull(context);
		this.context = context;
	}

	public ServletContextHandler getContext() {
		return context;
	}
}
