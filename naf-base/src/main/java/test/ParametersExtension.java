package test;

import java.util.Arrays;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Inject;

import org.jboss.weld.environment.se.beans.ParametersFactory;
import org.jboss.weld.environment.se.bindings.Parameters;

/**
 * Replace the {@link ParametersFactory} from Weld by our own injection.
 * 
 * @author pms1
 *
 */
class ParametersExtension implements com.github.naf.spi.Extension {
	private final String[] args;

	ParametersExtension() {
		this.args = null;
	}

	ParametersExtension(String[] args) {
		Objects.requireNonNull(args);
		this.args = args;
	}

	String[] getArgs() {
		return args;
	}

	void disableWeldParametersFactory(@Observes ProcessAnnotatedType<ParametersFactory> pat) {
		pat.veto();
	}

	void disableOurParametersFactory(@Observes ProcessAnnotatedType<OurParametersFactory> pat) {
		if (args == null)
			pat.veto();
	}

	@ApplicationScoped
	static class OurParametersFactory {
		@Inject
		ParametersExtension e;

		@Produces
		@Parameters
		String[] foo() {
			return e.getArgs();
		}
	}

	@Override
	public String toString() {
		return super.toString() + "(args=" + Arrays.toString(args) + ")";
	}
}
