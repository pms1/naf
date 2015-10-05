package com.github.naf.samples;

import test.Application;
import test.ApplicationBuilder;

public class SampleMain {
	public static void main(String[] args) {
		try (Application a = new ApplicationBuilder().build()) {
			a.get(HelloService.class).hello();
		}
	}
}
