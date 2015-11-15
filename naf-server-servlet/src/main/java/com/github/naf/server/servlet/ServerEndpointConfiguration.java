package com.github.naf.server.servlet;

import java.net.InetAddress;

public class ServerEndpointConfiguration {
	private int port = 0;

	private InetAddress address = InetAddress.getLoopbackAddress();

	public ServerEndpointConfiguration withPort(int port) {
		if (port < 0 || port > 0xffff)
			throw new IllegalArgumentException();
		this.port = port;
		return this;
	}

	public ServerEndpointConfiguration withAddress(InetAddress address) {
		this.address = address;
		return this;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
}
