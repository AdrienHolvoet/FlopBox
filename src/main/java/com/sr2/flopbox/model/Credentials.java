package com.sr2.flopbox.model;

/**
 * Class which represents the identifiers of a user of the flopbox platform, in
 * order to allow him to modify / add / delete servers available on the platform
 * 
 * @author Adrien Holvoet
 */
public class Credentials {
	// All private because there are all only used inside the class
	private String username;
	private String password;

	/**
	 * Default constructor
	 */
	public Credentials() {
	}

	/**
	 * Constructor
	 * 
	 * @param username
	 * @param password
	 */
	public Credentials(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
