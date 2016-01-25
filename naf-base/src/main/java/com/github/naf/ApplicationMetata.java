package com.github.naf;

import java.util.Objects;

import javax.enterprise.inject.spi.Extension;

class ApplicationMetata implements Extension {
	private final String appName;

	public ApplicationMetata(String appName) {
		Objects.requireNonNull(appName);

		this.appName = appName;
	}

	String getApplicationName() {
		return appName;
	}
}
