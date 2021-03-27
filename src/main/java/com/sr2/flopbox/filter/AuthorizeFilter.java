package com.sr2.flopbox.filter;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sr2.flopbox.exception.NotAuthorizedException;
import com.sr2.flopbox.model.Credentials;
import com.sr2.flopbox.model.ErrorResponse;
import com.sr2.flopbox.service.AuthenticationService;
import com.sr2.flopbox.service.JwtTokenService;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * This class is a filter and check the request headers. Basically, when a
 * endpoint with the annotation Authorize is invoked, the runtime intercepts the
 * invocation, and does the following: 
 * 1) Gets the HTTP Authorization headerfrom
 * the request and checks for the JSon Web Token (the Bearer string) 
 * 2) Itvalidates the token (using the JJWT library) 
 * 3) If the token is valid, the Authorize method is invoked 
 * 4) If something went wrong(token invalid=, a 401 Unauthorized is sent to the client
 * 
 * @author Adrien Holvoet
 */
@Provider
@Authorize
public class AuthorizeFilter implements ContainerRequestFilter {
	private static final Logger logger = LogManager.getLogger(AuthorizeFilter.class);
	private JwtTokenService jwtTokenService = JwtTokenService.getInstance();
	private AuthenticationService authenticationService = AuthenticationService.getInstance();

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		try {
			// Get the HTTP Authorization header from the request
			String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
			if (authorizationHeader == null || !(authorizationHeader.startsWith("Bearer"))) {
				throw new NotAuthorizedException("WWW-Authenticate=Bearer");
			}

			// Extract the token from the HTTP Authorization header
			String token = authorizationHeader.substring("Bearer".length()).trim();

			Credentials credentials = jwtTokenService.decodeJWT(token);

			// Throws a exception if the user doesn't exist
			authenticationService.checkIfUserExist(credentials);

		} catch (Exception e) {
			logger.error("Status : " + Response.Status.UNAUTHORIZED + ", Message : " + e.toString());
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
					.entity(new ErrorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), e.getMessage())).build());
		}
	}

}
