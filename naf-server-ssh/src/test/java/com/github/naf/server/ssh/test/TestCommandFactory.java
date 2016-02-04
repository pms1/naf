package com.github.naf.server.ssh.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;

import com.github.naf.server.ssh.CommandRequestScopeBinder;
import com.github.naf.server.ssh.CommandRequestScopeBinding;

@ApplicationScoped
public class TestCommandFactory implements CommandFactory {

	static List<String> called = Collections.synchronizedList(new LinkedList<>());

	@Dependent
	static class DependentBean {
		@PostConstruct
		void pc() {
			called.add("dependent-post-construct");
		}

		@PreDestroy
		void pd() {
			called.add("dependent-pre-destroy");
		}
	}

	@Dependent
	static class DependentBean2 {
		@PostConstruct
		void pc() {
			called.add("dependent2-post-construct");
		}

		@PreDestroy
		void pd() {
			called.add("dependent2-pre-destroy");
			new Throwable().printStackTrace();
		}
	}

	@RequestScoped
	static class RequestScopedBean {
		@PostConstruct
		void pc() {
			called.add("request-post-construct");
		}

		@PreDestroy
		void pd() {
			called.add("request-pre-destroy");
		}

		void called(String text) {
			called.add("request-called-" + text);
		}
	}

	@RequestScoped
	static class RequestScopedBean2 {
		@PostConstruct
		void pc() {
			called.add("request2-post-construct");
		}

		@PreDestroy
		void pd() {
			called.add("request2-pre-destroy");
		}

		void called(String text) {
			called.add("request2-called-" + text);
		}
	}

	@Inject
	DependentBean dcf;

	@Inject
	RequestScopedBean rcf;

	@Inject
	CommandRequestScopeBinder tool;

	static byte[] testOutputStdout = "stdout\n".getBytes();
	static byte[] testOutputStderr = "stderr\n".getBytes();

	class TestCommand implements Command, SessionAware {

		@Override
		public void setInputStream(InputStream in) {
		}

		OutputStream stdoutStream;

		@Override
		public void setOutputStream(OutputStream out) {
			stdoutStream = out;
		}

		OutputStream stderrStream;

		@Override
		public void setErrorStream(OutputStream err) {
			stderrStream = err;
		}

		private ExitCallback ec;

		@Override
		public void setExitCallback(ExitCallback callback) {
			ec = callback;
		}

		@Inject
		DependentBean2 dc;

		@Inject
		RequestScopedBean2 rc;

		@Override
		public void start(Environment env) throws IOException {

			called.add("command-start");
			rcf.called("command-start");
			rc.called("rc-command-start");
			Command t = this;

			new Thread() {
				@Override
				public void run() {

					called.add("command-thread-run");

					try {
						rcf.called("command-thread-run-before-associate-ok");
					} catch (ContextNotActiveException e) {
						called.add("command-thread-run-before-associate-fail");
					}

					try {
						rc.called("rc-command-thread-run-before-associate-ok");
					} catch (ContextNotActiveException e) {
						called.add("rc-command-thread-run-before-associate-fail");
					}

					try (CommandRequestScopeBinding assosiate = tool.associate(t)) {

						rcf.called("command-before-send");
						rc.called("rc-command-before-send");
						try {
							stdoutStream.write(testOutputStdout);
							stdoutStream.flush();
							stderrStream.write(testOutputStderr);
							stderrStream.flush();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							ec.onExit(0, "ok");
						}

						rcf.called("command-before-wait-after-send");

						try {
							Thread.sleep(2000);
						} catch (Throwable e) {
						}

						rcf.called("command-after-wait-after-send");
					}

					try {
						rcf.called("command-thread-run-after-associate-ok");
					} catch (ContextNotActiveException e) {
						called.add("command-thread-run-after-associate-fail");
					}

					try (CommandRequestScopeBinding token = tool.associate(t)) {
						called.add("command-thread-run-re-associate-ok");
					} catch (Throwable t) {
						called.add("command-thread-run-re-associate-fail");
					}

					sync.set(true);
					synchronized (sync) {
						sync.notify();
					}
				}
			}.start();

		}

		@Override
		public void destroy() {
			called.add("command-destroy");
		}

		@Override
		public void setSession(ServerSession session) {
			called.add("command-set-session");
		}

	}

	@Override
	public Command createCommand(String command) {

		called.add("factory-create-command");

		return new TestCommand();
	}

	@PostConstruct
	void pc() {
		called.add("command-factory-post-construct");
	}

	@PreDestroy
	void pd() {
		called.add("command-factory-pre-destroy");
	}

	static AtomicBoolean sync = new AtomicBoolean(false);
}
