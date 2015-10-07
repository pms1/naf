package test;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

@ApplicationScoped
public class X {
	@Inject
	@Any
	Event<AfterBootEvent> x;

	void fire() {
		x.fire(new AfterBootEvent());
	}
}
