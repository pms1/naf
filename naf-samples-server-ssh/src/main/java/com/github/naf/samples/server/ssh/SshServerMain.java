package com.github.naf.samples.server.ssh;

import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;
import com.github.naf.server.ServerEndpointConfiguration;
import com.github.naf.server.ssh.SshServerConfiguration;

public class SshServerMain {
	static Object config = new SshServerConfiguration().withEndpoint(new ServerEndpointConfiguration().withPort(2223));

	public static void main(String[] args) throws InterruptedException {
		try (Application a = new ApplicationBuilder().with(config).build()) {

			Thread.sleep(100 * 1000);
			System.err.println("done");

			Thread.getAllStackTraces().keySet().forEach(System.err::println);
		}
		System.err.println("done2");

		Thread.getAllStackTraces().keySet().forEach(System.err::println);
	}
}
