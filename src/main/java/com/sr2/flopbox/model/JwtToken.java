package com.sr2.flopbox.model;

/**
 * Class which represents a jwt token
 * 
 * @author Adrien Holvoet
 * 
 */
public class JwtToken {

	// private because it is only used inside the class
	private String token;

	/**
	 * Default constructor
	 */
	public JwtToken() {
	}

	/**
	 * Constructor
	 * 
	 * @param token
	 */
	public JwtToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
