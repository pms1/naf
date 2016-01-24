package com.github.naf.samples.server.ssh;

import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;

public class SshServerMain {
	public static void main(String[] args) throws InterruptedException {
		try (Application a = new ApplicationBuilder().build()) {

			Thread.sleep(100 * 1000);
			System.err.println("done");

			Thread.getAllStackTraces().keySet().forEach(System.err::println);
		}
		System.err.println("done2");

		Thread.getAllStackTraces().keySet().forEach(System.err::println);
	}
}
