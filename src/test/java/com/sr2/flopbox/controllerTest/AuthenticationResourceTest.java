package com.sr2.flopbox.controllerTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sr2.flopbox.Main;
import com.sr2.flopbox.common.Constant;
import com.sr2.flopbox.model.Credentials;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class AuthenticationResourceTest {

	// static because same instance for all
	private static HttpServer server;
	private static WebTarget target;
	private static String resourceName;

	@BeforeClass
	public static void setUp() throws Exception {
		// start the server
		server = Main.startServer();

		// create the client
		resourceName = "authentication";
		Client c = ClientBuilder.newClient();
		target = c.target(Constant.BASE_URI + resourceName);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		server.stop();
	}

	@Test
	public void authenticatePOST_shouldReturn200CodeAndToken_whenCorrectCredentials() {

		Credentials correctCredentials = new Credentials("root", "root");

		Response response = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.post(Entity.json(correctCredentials));
		String entity = response.readEntity(String.class);

		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertNotNull(entity);
	}

	@Test
	public void authenticatePOST_shouldReturn401CodeAndErrorResponse_whenWrongCredentials() {

		Credentials wrongCredentials = new Credentials("wrong", "wrong");

		Response response = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.post(Entity.json(wrongCredentials));
		String entity = response.readEntity(String.class);

		assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
		assertEquals("{\"message\":\"The credentials are wrong\",\"statusCode\":401}", entity);
	}

	@Test
	public void authenticatePOST_shouldReturn400CodeAndErrorResponse_whenNullCredentials() {

		Credentials nullCredentials = new Credentials("", "");

		Response response = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.post(Entity.json(nullCredentials));

		String entity = response.readEntity(String.class);

		assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		assertEquals("{\"message\":\"The credentials cannot be null\",\"statusCode\":400}", entity);
	}
}
