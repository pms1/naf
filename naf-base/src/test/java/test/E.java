package test;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import com.github.naf.spi.Extension;

public class E implements Extension {

	{
		System.err.println("CONS");
	}

	<T, X> void obs(@Observes ProcessInjectionPoint<T, X> pip) {
		System.err.println("PIP " + pip.getInjectionPoint());
	}

	<X> void obs(@Observes ProcessInjectionTarget<X> pit) {
		System.err.println("PIT " + pit.getAnnotatedType());
		System.err.println("PIT " + pit.getInjectionTarget());
	}
}
