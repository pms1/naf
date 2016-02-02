package com.github.naf.server.ssh;

import java.util.Objects;

import com.github.naf.server.ssh.NAFExtension.CommandContext;

public class CommandRequestScopeBinding implements AutoCloseable {
	final CommandContext e;

	CommandRequestScopeBinding(CommandContext e) {
		Objects.requireNonNull(e);
		this.e = e;
	}

	public void close() {
		e.detach();
	}
}