package com.github.naf.samples.jta;

import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;

public class JtaSampleMain {
	public static void main(String[] args) {
		try (Application a = new ApplicationBuilder().build()) {
			a.get(JtaHelloService.class).hello();
			a.get(JtaHelloService.class).hello2();
		}
	}
}
