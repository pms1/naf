
import test.Application;
import test.ApplicationBuilder;

public class ServletSampleMain {
	public static void main(String[] args) {
		try (Application a = new ApplicationBuilder().build()) {
			a.join();
		}
	}
}
