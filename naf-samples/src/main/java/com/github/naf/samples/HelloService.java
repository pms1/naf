package com.github.naf.samples;

import java.util.Arrays;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.weld.environment.se.bindings.Parameters;

@Dependent
public class HelloService {
	@Inject
	@Parameters
	String[] args;

	void hello() {
		System.out.println("hello, world\nargs=" + Arrays.toString(args));
	}
}
