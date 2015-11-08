package com.github.naf.samples;

import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;

public class SampleMain {
	public static void main(String[] args) {
		try (Application a = new ApplicationBuilder().build()) {
			a.get(HelloService.class).hello();
		}
	}
}
