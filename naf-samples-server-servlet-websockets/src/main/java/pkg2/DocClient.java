package pkg2;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

public class DocClient {
	private static CountDownLatch messageLatch;
	private static final String SENT_MESSAGE = "Hello World";

	public static void main(String[] args) {
		try {
			messageLatch = new CountDownLatch(1);

			final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

			ClientManager client = ClientManager.createClient();
			client.connectToServer(new Endpoint() {

				@Override
				public void onOpen(Session session, EndpointConfig config) {
					try {
						session.addMessageHandler(new MessageHandler.Whole<String>() {

							@Override
							public void onMessage(String message) {
								System.out.println("Received message: " + message);
								messageLatch.countDown();
							}
						});
						session.getBasicRemote().sendText(SENT_MESSAGE);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, cec, new URI("ws://localhost:8080/echo"));
			messageLatch.await(100, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}