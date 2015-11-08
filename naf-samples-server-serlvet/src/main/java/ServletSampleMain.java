
import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;

public class ServletSampleMain {
	public static void main(String[] args) {
		try (Application a = new ApplicationBuilder().build()) {
			a.join();
		}
	}
}
