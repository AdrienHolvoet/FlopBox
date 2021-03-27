package com.sr2.flopbox;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sr2.flopbox.common.Constant;

/**
 * Application entry point
 *
 * @author Adrien Holvoet
 */
public class Main {

	/**
	 * Application entry point which starts Grizzly HTTP server exposing JAX-RS
	 * resources defined in this application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public static HttpServer startServer() {
		// create a resource config that scans for JAX-RS resources and providers
		// in com.sr2.flopbox package
		final ResourceConfig rc = new ResourceConfig().packages(Constant.PACKAGE_PATH);

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		return GrizzlyHttpServerFactory.createHttpServer(URI.create(Constant.BASE_URI), rc);
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final HttpServer server = startServer();
		System.out.println(String.format("Flopbox application is available at " + "%s\nHit enter to stop it...",
				Constant.BASE_URI));
		System.in.read();
		server.stop();
	}
}
