package com.github.naf.server.ssh.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.stream.Collectors;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.PublicKeyEntry;
import org.apache.sshd.common.util.io.NoCloseInputStream;
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

	@Test
	public void injection() throws InterruptedException, IOException, Throwable {
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();

		try (Application a = new ApplicationBuilder().with(config).build()) {

			try (SshClient client = SshClient.setUpDefaultClient()) {
				client.start();

				String privateKeybody = Files
						.readAllLines(PublicKeyEntry.getDefaultKeysFolder().toPath().resolve("id_rsa")).stream()
						.collect(Collectors.joining("\n"));
				String publicKeybody = Files
						.readAllLines(PublicKeyEntry.getDefaultKeysFolder().toPath().resolve("id_rsa.pub")).stream()
						.collect(Collectors.joining("\n"));

				SshRsaCrypto rsa = new SshRsaCrypto();
				PublicKey publicKey = rsa.readPublicKey(rsa.slurpPublicKey(publicKeybody));
				PrivateKey privateKey = rsa.readPrivateKey(rsa.slurpPrivateKey(privateKeybody));

				KeyPair keyPair = new KeyPair(publicKey, privateKey);

				try (ClientSession session = client
						.connect(System.getProperty("user.name"), "localhost", config.getEndpoint().getPort()).await()
						.getSession()) {
					session.addPublicKeyIdentity(keyPair);
					session.auth().verify();

					try (ClientChannel channel = session.createChannel("exec", "foo")) {
						channel.setIn(new NoCloseInputStream(System.in));
						channel.setOut(stdout);
						channel.setErr(stderr);
						channel.open();
						channel.waitFor(ClientChannel.CLOSED, 0);
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

		assertThat(TestCommandFactory.called,
				IsIterableContainingInAnyOrder.containsInAnyOrder("dependent-post-construct",
						"command-factory-post-construct", "factory-create-command", "command-start",
						"request-post-construct", "request-called-command-start", "command-thread-run",
						"command-thread-run-before-associate-fail", "request-called-command-before-send",
						"request-called-command-before-wait-after-send", "command-destroy",
						"request-called-command-after-wait-after-send", "request-pre-destroy",
						"command-factory-pre-destroy", "dependent-pre-destroy", "dependent-pre-destroy",
						"command-thread-run-after-associate-fail", "command-thread-run-re-associate-fail"));
	}
}
