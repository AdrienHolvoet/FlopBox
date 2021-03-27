package com.sr2.flopbox.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.sr2.flopbox.common.CheckUtils;
import com.sr2.flopbox.common.Constant;
import com.sr2.flopbox.exception.NotAuthorizedException;
import com.sr2.flopbox.model.Credentials;
import com.sr2.flopbox.model.JwtToken;

import jakarta.ws.rs.BadRequestException;

/**
 * Class Singleton which contains all the logic linked to the Authentication
 * resource. This is used so that a user can connect to the platform flopBox.
 * 
 * @author Adrien Holvoet
 * 
 */
public class AuthenticationService {
	/*
	 * All Private because the are only used in this class and static because it is
	 * unique
	 */
	private static AuthenticationService instance;
	private JwtTokenService jwtTokenService = JwtTokenService.getInstance();

	/**
	 * Private constructor to prevent instantiation
	 */
	private AuthenticationService() {

	}

	/**
	 * Static method to return the unique AuthenticationService instance, create it
	 * if it does not exist
	 * 
	 * @return instance a instance of AuthenticationService
	 */
	public static AuthenticationService getInstance() {
		if (instance == null) {
			instance = new AuthenticationService();
		}
		return instance;
	}

	/**
	 * 
	 * Method used to check to return a jwt token if the user exist
	 * 
	 * @param credentials credentials of the user
	 * 
	 * @return JwtToken jwtToken used to have access to Authorize resources
	 * 
	 * @throws FileNotFoundException
	 * @throws NotAuthorizedException 
	 * @throws BadRequestException   if the credentials are empty
	 */
	public JwtToken authenticate(Credentials credentials) throws FileNotFoundException, NotAuthorizedException {
		if (!CheckUtils.checkIfStringIsNull(credentials.getUsername())
				|| !CheckUtils.checkIfStringIsNull(credentials.getPassword())) {
			throw new BadRequestException("The credentials cannot be null");
		}

		// Throws a exception if the user doesn't exist
		this.checkIfUserExist(credentials);

		return jwtTokenService.generateToken(credentials);
	}

	/**
	 * 
	 * Method used to check if the identifiers passed in parameters correspond to an
	 * existing user.
	 * 
	 * @param credentials credentials of the user
	 * 
	 * @return true if exist false otherwise
	 * 
	 * @throws FileNotFoundException
	 * @throws NotAuthorizedException     if the user doesn't exist
	 * 
	 */
	public boolean checkIfUserExist(Credentials credentials) throws FileNotFoundException, NotAuthorizedException {
		// Input file
		FileInputStream file;

		file = new FileInputStream(Constant.USER_DB);
		Scanner scanner = new Scanner(file);

		while (scanner.hasNextLine()) {
			if (scanner.nextLine().equals(credentials.getUsername())) {
				if (scanner.nextLine().equals(credentials.getPassword())) {
					scanner.close();
					return true;

				}
				scanner.close();
				throw new NotAuthorizedException("The credentials are wrong");
			}
			scanner.nextLine();
		}
		scanner.close();
		throw new NotAuthorizedException("The credentials are wrong");

	}
}
