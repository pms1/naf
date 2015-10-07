package pkg;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("foo")
@RequestScoped
public class ResXXX {
	@Inject
	Foo foo;

	public ResXXX() {
		System.err.println("NEW RES");
	}

	@GET
	public String doIt() {
		return "hello " + this + " " + foo;
	}

	@GET
	@Path("foo/{path: .+}")
	public String doIt2(@PathParam("path") String path) {
		return "hello2 " + path;
	}

	@GET
	@Path("foo/{path: .+}/changes")
	public String doIt3(@PathParam("path") String path) {
		return "hello2 " + path;
	}
}
