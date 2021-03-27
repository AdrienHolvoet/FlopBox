package com.sr2.flopbox.exception;

/**
 * Exception throw when an unexpected status code is sent by the ftp server
 * 
 * @author Adrien Holvoet
 */
public class UnexpectedFtpStatusCodeException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message Exception message
	 */
	public UnexpectedFtpStatusCodeException(String message) {
		super(message);
	}
}