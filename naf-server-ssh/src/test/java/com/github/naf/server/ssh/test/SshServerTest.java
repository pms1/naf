package com.github.naf.server.ssh.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.PublicKeyEntry;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import com.github.fommil.ssh.SshRsaCrypto;
import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;
import com.github.naf.server.ServerEndpointConfiguration;
import com.github.naf.server.ssh.SshServerConfiguration;

public class SshServerTest {

	static final int port;

	static {
		try (ServerSocket s = new ServerSocket(0)) {
			port = s.getLocalPort();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static SshServerConfiguration config = new SshServerConfiguration()
			.withEndpoint(new ServerEndpointConfiguration().withPort(port));

	@Dependent
	static class A {
		@Inject
		Event<TestEvent> e;

		void fire() {
			System.err.println("FIRE");
			e.fire(new TestEvent());
		}
	}

	static class TestInputStream extends InputStream {
		volatile boolean w = false;

		boolean first = false;

		@Override
		public int read() throws IOException {

			synchronized (this) {
				try {
					System.err.println("WAIT");
					w = true;
					this.wait();
					System.err.println("WAIT DONE");
				} catch (InterruptedException e) {
					System.err.println("E ");
				}
			}

			if (first) {
				first = false;
				return 'x';
			} else {
				return -1;
			}
		}

	}

	@Test
	public void injection() throws InterruptedException, IOException, Throwable {
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();

		try (Application a = new ApplicationBuilder().with(config).build()) {

			try (SshClient client = SshClient.setUpDefaultClient()) {
				client.start();

				String privateKeybody = Files.readAllLines(PublicKeyEntry.getDefaultKeysFolderPath().resolve("id_rsa"))
						.stream().collect(Collectors.joining("\n"));
				String publicKeybody = Files
						.readAllLines(PublicKeyEntry.getDefaultKeysFolderPath().resolve("id_rsa.pub")).stream()
						.collect(Collectors.joining("\n"));

				SshRsaCrypto rsa = new SshRsaCrypto();
				PublicKey publicKey = rsa.readPublicKey(rsa.slurpPublicKey(publicKeybody));
				PrivateKey privateKey = rsa.readPrivateKey(rsa.slurpPrivateKey(privateKeybody));

				KeyPair keyPair = new KeyPair(publicKey, privateKey);

				ConnectFuture connect = client.connect(System.getProperty("user.name"), "localhost",
						config.getEndpoint().getPort());
				connect.await();

				try (ClientSession session = connect.getSession()) {
					session.addPublicKeyIdentity(keyPair);
					session.auth().verify();

					try (ClientChannel channel = session.createChannel("exec", "foo")) {
						TestInputStream in = new TestInputStream();
						channel.setIn(in);
						channel.setOut(stdout);
						channel.setErr(stderr);
						channel.open();
						System.err.println("O ");
						while (!in.w)
							;

						a.get(A.class).fire();

						synchronized (in) {
							in.notify();
						}
						System.err.println("O2 ");
						channel.waitFor(Collections.singleton(ClientChannelEvent.CLOSED), 0);
					} finally {
						session.close(false);
					}

				} finally {
					client.stop();
				}

				while (!TestCommandFactory.sync.get())
					synchronized (TestCommandFactory.sync) {
						TestCommandFactory.sync.wait();
					}
			}
		}

		assertThat(stdout.toByteArray(), equalTo(TestCommandFactory.testOutputStdout));
		assertThat(stderr.toByteArray(), equalTo(TestCommandFactory.testOutputStderr));

		TestCommandFactory.called.forEach(System.out::println);

		assertThat(TestCommandFactory.called, IsIterableContainingInAnyOrder.containsInAnyOrder(
				"dependent-post-construct", "command-factory-post-construct", "factory-create-command", "command-start",
				"request-post-construct", "request-called-command-start", "command-thread-run",
				"command-thread-run-before-associate-fail", "request-called-command-before-send",
				"request-called-command-before-wait-after-send", "command-destroy",
				"request-called-command-after-wait-after-send", "request-pre-destroy", "command-factory-pre-destroy",
				"dependent-pre-destroy", /* "dependent-pre-destroy", */ "command-thread-run-after-associate-fail",
				"command-thread-run-re-associate-fail", "rc-command-thread-run-before-associate-fail", //
				//
				"dependent2-post-construct", "dependent2-pre-destroy", //
				"dependent2-post-construct", "dependent2-pre-destroy",
				//
				"request2-post-construct", "request2-called-rc-command-start", "request2-called-rc-command-before-send",
				"request2-pre-destroy",
				//
				"command-set-session"
		//
		));
	}
}
