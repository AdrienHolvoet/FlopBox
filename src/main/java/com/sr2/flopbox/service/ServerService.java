package com.sr2.flopbox.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sr2.flopbox.common.CheckUtils;
import com.sr2.flopbox.common.Constant;
import com.sr2.flopbox.model.Server;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

/**
 * Class Singleton which contains all the logic linked to the Server resource
 * used to perform CRUD operations. All method are static because they are the
 * same for every instance of the class
 * 
 * @author Adrien Holvoet
 * 
 */
public final class ServerService {
	// Private because it is only used in this class and static because it is unique
	private static ServerService instance;

	/**
	 * Private constructor to prevent instantiation
	 */
	private ServerService() {

	}

	/**
	 * Static method to return the unique serverService instance, create it if it
	 * does not exist
	 * 
	 * @return instance a instance of ServerService
	 */
	public static ServerService getInstance() {
		if (instance == null) {
			instance = new ServerService();
		}
		return instance;
	}

	/**
	 * 
	 * Method used to return the server requested by the client based on its alias
	 * 
	 * @param alias used to get the server corresponding
	 * 
	 * @return server
	 * 
	 * @throws IOException         if an error is raised when reading the file
	 *                             containing the servers
	 * @throws NotFoundException   if the server requested does not exist
	 * @throws BadRequestException if the alias is null
	 */
	public Server getServer(String alias) throws IOException {
		if (!CheckUtils.checkIfStringIsNull(alias)) {
			throw new BadRequestException("The alias cannot be null");
		}
		// Input File
		File file = new File(Constant.SERVER_DB);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;
		while ((line = br.readLine()) != null) {
			if (line.split(Constant.SEPARATOR)[0].equals(alias)) {
				br.close();
				return convertTupleToServer(line);
			}
		}
		fr.close();
		throw new NotFoundException("The server requested doesn't exist on the FlopBox application");
	}

	/**
	 * Get all the servers registered on the FlopBox platform
	 * 
	 * @return instance a instance of ServerService
	 * 
	 * @throws IOException       if an error is raised when reading the file
	 *                           containing the servers
	 * @throws NotFoundException if the file is empty
	 */
	public List<Server> getServers() throws IOException {
		List<Server> servers = new ArrayList<Server>();
		// Input File
		File file = new File(Constant.SERVER_DB);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;
		while ((line = br.readLine()) != null) {
			servers.add(convertTupleToServer(line));
		}
		fr.close();

		if (servers.size() == 0) {
			throw new NotFoundException("No resources available please use POST method");
		}
		return servers;
	}

	/**
	 * Create a server on the database(file)
	 * 
	 * @param server server to create
	 * 
	 * @return instance a instance of ServerService
	 * 
	 * @throws IOException         if an error is raised when writing on the file
	 *                             containing the servers
	 * @throws BadRequestException when the value sent by the client does not
	 *                             respect the expected format
	 */
	public Server createServer(Server server) throws IOException {
		if (!CheckUtils.checkIfStringIsNull(server.getAlias())
				|| !CheckUtils.checkIfStringIsNull(server.getAddress())) {
			throw new BadRequestException("QueryParams cannot be null");
		}

		if (!CheckUtils.checkIfStringIsInCorrectFormat(server.getAlias())
				|| !CheckUtils.checkIfStringIsInCorrectFormat(server.getAddress())) {
			throw new BadRequestException("QueryParams cannot contain ' " + Constant.SEPARATOR + " '");
		}

		final Path path = Paths.get(Constant.SERVER_DB);
		Files.write(path, Arrays.asList(convertServerToTuple(server)), StandardCharsets.UTF_8,
				Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);

		return server;
	}

	/**
	 * Synchronized method allowing only one thread to update a server on the
	 * database(file). Like that it's sure that two people cannot update the same
	 * server à the same time(avoid conflict)
	 * 
	 * @param server server to update
	 * @param alias  alias of the server to update
	 * 
	 * @throws IOException         if an error is raised when writing on the file
	 *                             containing the servers
	 * @throws BadRequestException when the value sent by the client does not
	 *                             respect the expected format
	 * @throws NotFoundException   if the server doesn't exist
	 * 
	 * @return server the server updated
	 */
	public synchronized Server updateServer(String alias, Server server) throws IOException {
		if (!CheckUtils.checkIfStringIsNull(server.getAlias())
				|| !CheckUtils.checkIfStringIsNull(server.getAddress())) {
			throw new BadRequestException("Params cannot be null");
		}

		if (!CheckUtils.checkIfStringIsInCorrectFormat(server.getAlias())
				|| !CheckUtils.checkIfStringIsInCorrectFormat(server.getAddress())) {
			throw new BadRequestException("Params cannot contain ' " + Constant.SEPARATOR + " '");
		}

		final Path path = Paths.get(Constant.SERVER_DB);
		List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

		Server oldServer = getServer(alias);
		for (int i = 0; i < fileContent.size(); i++) {
			if (fileContent.get(i).equals(convertServerToTuple(oldServer))) {
				fileContent.set(i, convertServerToTuple(server));
				Files.write(path, fileContent, StandardCharsets.UTF_8);
				return server;
			}
		}
		throw new NotFoundException("The server requested doesn't exist on the FlopBox application");
	}

	/**
	 * Synchronized method allowing only one thread to delete a server on the
	 * database(file). Like that it's sure that two people cannot delete the same
	 * server à the same time(avoid conflict)
	 * 
	 * @param alias server to delete
	 * 
	 * @throws IOException       if an error is raised when writing on the file
	 *                           containing the servers
	 * @throws NotFoundException if the server doesn't exist
	 * 
	 * @return server the updated server
	 */
	public synchronized boolean deleteServer(String alias) throws IOException {

		final Path path = Paths.get(Constant.SERVER_DB);
		List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

		Server oldServer = getServer(alias);

		for (int i = 0; i < fileContent.size(); i++) {
			if (fileContent.get(i).equals(convertServerToTuple(oldServer))) {
				fileContent.remove(i);
				Files.write(path, fileContent, StandardCharsets.UTF_8);
				return true;
			}
		}
		throw new NotFoundException("The server requested doesn't exist on the FlopBox application");
	}

	/**
	 * Private method because only used in this class used to convert a row from the
	 * file into a Server object
	 * 
	 * @param tuple a line of the file which can be compared to a tuple in database
	 * 
	 * @return Server object
	 */
	private Server convertTupleToServer(String tuple) {
		String[] parts = tuple.split(Constant.SEPARATOR);
		return new Server(parts[0], parts[1], Integer.parseInt(parts[2]));
	}

	/**
	 * Private method because only used in this class used to convert a server
	 * object in a string in the appropriate format to save it in the file
	 * 
	 * @param tuple a line of the file which can be compared to a tuple in database
	 * 
	 * @return Server object
	 */
	private String convertServerToTuple(Server server) {
		return server.getAlias() + Constant.SEPARATOR + server.getAddress() + Constant.SEPARATOR + server.getPort();
	}

}
