package com.github.naf.server.ssh;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.sshd.server.Command;

@Dependent
public class CommandRequestScopeBinder {
	@Inject
	NAFExtension e;

	public CommandRequestScopeBinding associate(Command t) {
		return e.associate(t);
	}

}