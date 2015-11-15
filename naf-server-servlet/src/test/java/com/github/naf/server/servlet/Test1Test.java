package com.github.naf.server.servlet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import com.github.naf.Application;
import com.github.naf.ApplicationBuilder;

public class Test1Test {
	@Test
	public void test() throws IOException {
		try (Application a = new ApplicationBuilder().build()) {
			URI base = a.get(URI.class);

			try (CloseableHttpClient c = HttpClients.createDefault()) {

				try (CloseableHttpResponse response = c.execute(new HttpGet(base))) {

					assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NOT_FOUND));
				}
			}
		}
	}
}
