package com.github.naf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import com.github.naf.spi.State;

@ApplicationScoped
public class StatePathProducer {

	@Inject
	ApplicationMetata am;

	@Produces
	@State
	@Dependent
	Path create(InjectionPoint p) {

		String os = System.getProperty("os.name");
		if (os.startsWith("Windows ")) {

		} else {
			throw new Error("Unhandled \"os.name\": os");
		}

		String env = System.getenv("LOCALAPPDATA");
		if (env == null || env.isEmpty())
			throw new Error();

		Path penv = Paths.get(env);
		if (!Files.exists(penv)) {
			throw new Error();
		}

		return penv.resolve(am.getApplicationName()).resolve("state")
				.resolve(p.getMember().getDeclaringClass().getName());
	}

}
