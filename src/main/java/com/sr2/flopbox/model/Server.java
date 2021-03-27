package com.sr2.flopbox.model;

/**
 * Class which represents a Server, it is characterized by an alias (to access
 * it), an address (ip address) and a port (port on which the server listens)
 * 
 * @author Adrien Holvoet
 * 
 */
public class Server {
	// All private because there are all only used inside the class
	private String alias;
	private String address;
	private int port;

	/**
	 * Default constructor
	 */
	public Server() {
	}

	/**
	 * Constructor
	 * 
	 * @param address
	 * @param port
	 * @param alias
	 */
	public Server(String alias, String address, int port) {
		this.address = address;
		this.port = port;
		this.alias = alias;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
