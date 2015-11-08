package com.github.naf.samples;

import java.util.Arrays;

import javax.inject.Inject;

import org.jboss.weld.environment.se.bindings.Parameters;

import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;

public class ParametersMain {
	static class Injected {
		@Inject
		@Parameters
		String[] params;

		String[] get() {
			return params;
		}
	}

	public static void main(String[] args) {
		try (Application a = new ApplicationBuilder().withParameters(args).build()) {
			System.err.println(Arrays.toString(a.get(Injected.class).get()));
		}
	}
}
