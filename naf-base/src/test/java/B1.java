import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class B1 {
	int b1Called = 0;
	int b2Called = 0;

	public void b1() {
		++b1Called;
		System.err.println("b1 " + this + " " + this.getClass() + " " + b1Called + " " + b2Called);
		b2();
	}

	void b2() {
		++b2Called;
		System.err.println("b2 " + this + " " + this.getClass() + " " + b1Called + " " + b2Called);
	}

	// void obs(@Observes String all) {
	// System.err.println("ALL " + all);
	// }

	@IB
	public void obs2(@Observes String all) {
		System.err.println("ALL2 " + all);
		System.err.println("all2 " + this + " " + this.getClass() + " " + b1Called + " " + b2Called);
		new Throwable().printStackTrace();
	}
}