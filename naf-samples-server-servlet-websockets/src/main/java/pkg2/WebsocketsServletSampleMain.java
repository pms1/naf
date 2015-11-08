package pkg2;

import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;

public class WebsocketsServletSampleMain {
	public static void main(String[] args) {
		try (Application a = new ApplicationBuilder().build()) {
			a.join();
		}
	}
}
