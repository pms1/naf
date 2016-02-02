package com.github.naf.samples.server.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import com.github.naf.server.ssh.CommandRequestScopeBinder;
import com.github.naf.server.ssh.CommandRequestScopeBinding;

public class SampleCommandFactory implements CommandFactory {

	@Dependent
	static class DependentBean {
		@PostConstruct
		void pc() {
			System.err.println("DEPENDENT BEAN " + this + " PostConstruct");
		}

		@PreDestroy
		void pd() {
			System.err.println("DEPENDENT BEAN " + this + " PreDestory");
		}
	}

	@RequestScoped
	static class RequestScopedBean {
		@PostConstruct
		void pc() {
			System.err.println("REQUEST SCOPED BEAN " + this + " PostConstruct");
		}

		@PreDestroy
		void pd() {
			System.err.println("REQUEST SCOPED BEAN " + this + " PreDestory");
		}
	}

	@Inject
	DependentBean s;

	@Inject
	RequestScopedBean b;

	@Inject
	CommandRequestScopeBinder tool;

	@Override
	public Command createCommand(String command) {

		System.err.println(this + " createCommand " + Thread.currentThread());
		System.err.println(this + " createCommand " + s + " " + s.getClass());
		System.err.println(this + " createCommand " + b + " " + b.getClass());

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

			void foo() {

			}

			@Override
			public void start(Environment env) throws IOException {

				System.err.println(this + " start " + Thread.currentThread());
				System.err.println(this + " start " + s + " " + s.getClass());
				System.err.println(this + " start " + b + " " + b.getClass());

				Command t = this;

				new Thread() {
					@Override
					public void run() {

						System.err.println(this + " run " + Thread.currentThread());

						try (CommandRequestScopeBinding assosiate = tool.associate(t)) {

							System.err.println(this + " run " + s + " " + s.getClass());
							System.err.println(this + " run " + b + " " + b.getClass());

							try {
								System.err.println("SLEEP-BEFORE");
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								System.err.println("SLEEP-BEFORE DONE");
								os.write("hello\n".getBytes());
								os.flush();
							} catch (IOException e) {
								e.printStackTrace();
							} finally {
								System.err.println("CALLING ON EXIT " + Thread.currentThread());
								ec.onExit(0, "ok");
								System.err.println("CALLED ON EXIT");
							}

							System.err.println("SLEEP-AFTER");
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							System.err.println("SLEEP-AFTER DONE");

						}

						try (CommandRequestScopeBinding assosiate = tool.associate(t)) {

						}
					}
				}.start();

			}

			@Override
			public void destroy() {
				System.err.println("DESTROY " + Thread.currentThread());
			}

		};
	}

	@PostConstruct
	void pc() {
		System.err.println("COMMAND FACTORY " + this + " PostConstruct");
	}

	@PreDestroy
	void pd() {
		System.err.println("COMMAND FACTORY " + this + " PreDestory");
	}

}
