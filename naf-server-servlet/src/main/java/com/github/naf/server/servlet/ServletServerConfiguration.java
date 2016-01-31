package com.github.naf.server.servlet;

import com.github.naf.server.ServerEndpointConfiguration;

public class ServletServerConfiguration {
	ServerEndpointConfiguration endpoint;

	public ServletServerConfiguration() {

	}

	public ServletServerConfiguration withEndpoint(ServerEndpointConfiguration endpoint) {
		this.endpoint = endpoint;
		return this;
	}
}
