package com.sr2.flopbox.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sr2.flopbox.exception.HandleException;
import com.sr2.flopbox.model.Credentials;
import com.sr2.flopbox.service.AuthenticationService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Authentication resource (exposed at "authentication" path) which allow a user
 * to generate a token if it is credential are good and therefore allow the use
 * of resources with the Authorize annotation
 *
 * @author Adrien Holvoet
 */

@Path("authentication")
public class AuthenticationResource {

	// All private because they are only used in this class
	private static final Logger logger = LogManager.getLogger(AuthenticationResource.class);
	private AuthenticationService authenticationService = AuthenticationService.getInstance();

	/**
	 * Method handling HTTP POST requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 * 
	 * @param credentials credentials of the user
	 *
	 * @return Response will be JwtToken generated from the credentials or an error
	 *         code with a message corresponding to the exception caught.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticate(Credentials credentials) {
		try {
			return Response.ok().entity(authenticationService.authenticate(credentials)).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		}
	}

}
