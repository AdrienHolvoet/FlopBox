package com.sr2.flopbox.common;

/**
 * Constant(Singleton pattern) contains all the constants used through the
 * flopBox API
 * 
 * @author Adrien Holvoet
 */

public class Constant {
	// All static final because there are all constants
	public static final String BASE_URI = "http://localhost:8080/flopbox/";
	public static final String PACKAGE_PATH = "com.sr2.flopbox";
	public static final String SERVER_DB = System.getProperty("user.dir") + "/src/main/resources/servers";
	public static final String USER_DB = System.getProperty("user.dir") + "/src/main/resources/users";
	public static final String SEPARATOR = ";";
	public static final String ANONYMOUS = "anonymous";
	public static final String PASSIVE = "passive";

	/**
	 * Prevent instantiation
	 */
	private Constant() {
	}

}
