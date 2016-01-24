package com.github.naf.samples.server.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

public class SampleShellFactory implements Factory<Command> {

	@Dependent
	static class SampleBean {
		@PostConstruct
		void pc() {
			System.err.println(this + " PC");
		}

		@PreDestroy
		void pd() {
			System.err.println(this + " PD");
		}
	}

	@Inject
	SampleBean s;

	@Override
	public Command create() {

		System.err.println(this + " CALLED " + s);

		return new Command() {

			@Override
			public void setInputStream(InputStream in) {
				System.err.println("SIS " + in);

			}

			OutputStream os;

			@Override
			public void setOutputStream(OutputStream out) {
				System.err.println("SOS " + out);
				os = out;
			}

			@Override
			public void setErrorStream(OutputStream err) {
				System.err.println("SES " + err);
			}

			private ExitCallback ec;

			@Override
			public void setExitCallback(ExitCallback callback) {
				System.err.println("SEC " + callback);
				ec = callback;
			}

			@Override
			public void start(Environment env) throws IOException {
				System.err.println("START " + env.getEnv());
				System.err.println("START " + env.getPtyModes());

				new Thread() {
					@Override
					public void run() {
						try {
							os.write("hello\n".getBytes());
							os.flush();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							ec.onExit(0, "ok");
						}

					}
				}.start();
			}

			@Override
			public void destroy() {
				System.err.println("DESTROY");
			}

		};
	}

	@PostConstruct
	void pc() {
		System.err.println(this + " PC");
	}

	@PreDestroy
	void pd() {
		System.err.println(this + " PD");
	}

}
