package com.github.naf.server.servlet.websockets;

public interface SessionBinding extends AutoCloseable {
	@Override
	void close();
}
