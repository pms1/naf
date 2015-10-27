package pkg;

import test.Application;
import test.ApplicationBuilder;

public class WebsocketsServletSampleMain {
	public static void main(String[] args) {
		try (Application a = new ApplicationBuilder().build()) {
			a.join();
		}
	}
}
