package com.github.naf.server.ssh;

import com.github.naf.server.ServerEndpointConfiguration;

public class SshServerConfiguration {
	ServerEndpointConfiguration endpoint;

	public SshServerConfiguration() {

	}

	public SshServerConfiguration withEndpoint(ServerEndpointConfiguration endpoint) {
		this.endpoint = endpoint;
		return this;
	}
}
