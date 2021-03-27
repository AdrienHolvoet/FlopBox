package com.sr2.flopbox.exception;

import org.apache.logging.log4j.Logger;

import com.sr2.flopbox.model.ErrorResponse;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * HandleException used to handle major exceptions / error
 * code with custom messages in a centralized location. They will be sent to the
 * user to warn them that something went wrong
 * 
 * @author Adrien Holvoet
 */
public final class HandleException {

	/**
	 * Private constructor to prevent instantiation
	 */
	private HandleException() {
	}

	/**
	 * Unique static method of HandleException which aims to return an error code
	 * with a personalized message to the user if an exception has been thrown
	 * 
	 * @param e      Exception raised
	 * @param logger The logger of the class where the exception has been thrown
	 * 
	 * @return The error code with a personalized message according to the exception
	 *         passed in the parameter
	 */
	public static final Response handleException(Exception e, Logger logger) {

		// if an unknown error occurs throws a 500 error
		Status status = Response.Status.INTERNAL_SERVER_ERROR;
		String message = "Something went wrong. Please try again later";
		if (e instanceof BadRequestException) {
			status = Response.Status.BAD_REQUEST;
			message = e.getMessage();
		}
		if (e instanceof NotFoundException) {
			status = Response.Status.NOT_FOUND;
			message = e.getMessage();
		}
		if (e instanceof NotAuthorizedException) {
			status = Response.Status.UNAUTHORIZED;
			message = e.getMessage();
		}
		if (e instanceof ForbiddenException) {
			status = Response.Status.FORBIDDEN;
			message = e.getMessage() + " You don't have enough Permission";
		}

		if (e instanceof NotAllowedException) {
			status = Response.Status.METHOD_NOT_ALLOWED;
			message = e.getMessage() + " You don't have enough Permission";
		}

		logger.error("Status : " + status + ", Message : " + e.toString());
		return Response.status(status).entity(new ErrorResponse(status.getStatusCode(), message.replace("\r\n", "")))
				.build();
	}
}
