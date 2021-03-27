package com.sr2.flopbox.model;

/**
 * Class which represents an error message sent to the client when the server
 * cannot respond to the request. It is characterised by an error
 * code(statusCode) and an informative message(message)
 * 
 * @author Adrien Holvoet
 *
 */
public class ErrorResponse {
	// All private because there are all only used inside the class
	private int statusCode;
	private String message;

	/**
	 * Default constructor
	 */
	public ErrorResponse() {
	}

	/**
	 * Constructor
	 * 
	 * @param statusCode
	 * @param message
	 */
	public ErrorResponse(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}

}
