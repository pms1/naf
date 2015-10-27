package pkg;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/echo")
@RequestScoped
public class EchoEndpointAnnotated {
	public EchoEndpointAnnotated() {
		System.err.println("WS CREATE " + this);
	}

	@Inject
	Foo foo;

	static {
		System.err.println("WS LOAD " + EchoEndpointAnnotated.class);
	}

	@OnMessage
	public String onMessage(String message, Session session) {
		System.err.println("WS ON MESSAGE " + this + " " + message + " " + session + " " + foo);
		return message;
	}
}