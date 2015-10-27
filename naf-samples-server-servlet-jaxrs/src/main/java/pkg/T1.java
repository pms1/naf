package pkg;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class T1 {
	void doIt() {
		System.err.println("do a request scoped thing");
	}
}
