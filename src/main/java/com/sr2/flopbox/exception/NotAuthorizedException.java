package com.sr2.flopbox.exception;

/**
 * Exception throw when the request has not been applied because it lacks valid authentication credentials for the target resource.
 * 
 * @author Adrien Holvoet
 */
public class NotAuthorizedException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message Exception message
	 */
	public NotAuthorizedException(String message) {
		super(message);
	}
}
