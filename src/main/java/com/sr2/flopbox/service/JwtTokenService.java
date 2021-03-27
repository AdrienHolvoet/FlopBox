package com.sr2.flopbox.service;

import javax.crypto.SecretKey;

import com.sr2.flopbox.model.Credentials;
import com.sr2.flopbox.model.JwtToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 *
 * the JwtTokenService (Singleton pattern) class is in charge of parsing the
 * token into credentials object and generating the token from the credentials
 * object. Also in charge to generate the secret key each time the server is
 * restarted
 *
 * @author Adrien Holvoet
 */
public class JwtTokenService {
	// All private because they are only used in this class and static because it is
	// unique
	private static JwtTokenService instance;
	private static SecretKey secretKey = null;

	/**
	 * Private constructor to prevent instantiation
	 */
	private JwtTokenService() {

	}

	/**
	 * Static method to return the unique JwtTokenService instance, create it if it
	 * does not exist
	 * 
	 * @return instance a instance of JwtTokenService
	 */
	public static JwtTokenService getInstance() {
		if (instance == null) {
			// generate a key for the server session used to encrypt / decrypt jwt tokens
			secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
			instance = new JwtTokenService();
		}
		return instance;
	}

	/**
	 * Tries to parse specified String as a JWT token. If successful, returns
	 * credentials object with username and mot de passe(extracted from token). If
	 * unsuccessful (token is invalid or not containing all required user
	 * properties), simply throw a exception catch later.
	 * 
	 * @param token the JWT token to parse
	 * 
	 * @return the credentials object extracted from specified token
	 */
	public Credentials decodeJWT(String token) {
		Jws<Claims> jwt = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
		Claims claims = jwt.getBody();

		return new Credentials(claims.get("username").toString().toString(), claims.get("password").toString());
	}

	/**
	 * Generates a JWT token containing username as subject, and username and
	 * password as additional claims. These properties are taken from the specified
	 * credentials object. Tokens validity is infinite.
	 * 
	 * @param credentials the user for which the token will be generated
	 * 
	 * @return the JWT token
	 */
	public JwtToken generateToken(Credentials credentials) {
		String jwtToken = Jwts.builder().claim("username", credentials.getUsername())
				.claim("password", credentials.getPassword()).setSubject("Authentication").signWith(secretKey)
				.compact();

		return new JwtToken(jwtToken);
	}
}
