package test;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.resources.spi.ResourceLoader;

class Main {

	static Weld createWeld() {
		try {
			InputStream loggingConfig = Main.class.getResourceAsStream("/logging.properties");
			if (loggingConfig != null)
				LogManager.getLogManager().readConfiguration(loggingConfig);
		} catch (SecurityException | IOException e) {
			throw new Error(e);
		}

		final Weld weld = new Weld() {
			@Override
			protected Deployment createDeployment(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
				Deployment deployment = super.createDeployment(resourceLoader, bootstrap);
				// deployment.getServices().add(TransactionServices.class,
				// Iterators.getOnlyElement(ServiceLoader.load(TransactionServices.class).iterator()));
				return deployment;
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				weld.shutdown();
			}
		});
		return weld;
	}
}
