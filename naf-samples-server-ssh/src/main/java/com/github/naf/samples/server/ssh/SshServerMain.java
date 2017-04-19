package com.github.naf.samples.server.ssh;

import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.PublicKeyEntry;
import org.apache.sshd.common.util.io.NoCloseInputStream;
import org.apache.sshd.common.util.io.NoCloseOutputStream;

import com.github.fommil.ssh.SshRsaCrypto;
import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;
import com.github.naf.server.ServerEndpointConfiguration;
import com.github.naf.server.ssh.SshServerConfiguration;

public class SshServerMain {
	static SshServerConfiguration config = new SshServerConfiguration()
			.withEndpoint(new ServerEndpointConfiguration().withPort(2223));

	public static void main(String[] args) throws InterruptedException, IOException, Throwable {
		try (Application a = new ApplicationBuilder().with(config).build()) {

			// SshClient client = new SshClient();
			// client.connect("Mirko", "localhost",
			// config.getEndpoint().getPort());

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
						channel.setIn(new NoCloseInputStream(System.in));
						channel.setOut(new NoCloseOutputStream(System.out));
						channel.setErr(new NoCloseOutputStream(System.err));
						channel.open();
						channel.waitFor(Collections.singleton(ClientChannelEvent.CLOSED), 0);
					} finally {
						session.close(false);
					}
				} finally {
					client.stop();
				}
			}

			System.err.println("WAIT");
			Thread.sleep(100 * 1000);
			System.err.println("done");

			Thread.getAllStackTraces().keySet().forEach(System.err::println);
		}
		System.err.println("done2");

		Thread.getAllStackTraces().keySet().forEach(System.err::println);
	}
}
