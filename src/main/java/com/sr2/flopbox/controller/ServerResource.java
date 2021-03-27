package com.sr2.flopbox.controller;

import jakarta.ws.rs.core.GenericEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sr2.flopbox.exception.HandleException;
import com.sr2.flopbox.filter.Authorize;
import com.sr2.flopbox.model.Server;
import com.sr2.flopbox.service.ServerService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Server resource (exposed at "server" path) which allow to get all (GET), get
 * (GET), create (POST), update( PUT) and delete (DELETE) a server
 *
 * @author Adrien Holvoet
 */

@Path("servers")
public class ServerResource {

	// All private because they are only used in this class
	private static final Logger logger = LogManager.getLogger(ServerResource.class);
	private ServerService serverService = ServerService.getInstance();

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 *
	 * @return Response will be all existing servers or an error code with a message
	 *         corresponding to the exception caught.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getServers() {
		try {
			GenericEntity entity = new GenericEntity<List<Server>>(serverService.getServers()){};
			return Response.ok(entity).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		}
	}

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 * 
	 * @param alias used to get the server corresponding
	 * 
	 * @return Response will be the server corresponding to the alias specified in
	 *         the path or an error code with a message corresponding to the
	 *         exception caught.
	 */
	@GET
	@Path("{alias}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getServer(@PathParam("alias") String alias) {
		try {
			return Response.ok(serverService.getServer(alias)).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		}
	}

	/**
	 * Method handling HTTP POST requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 * 
	 * @param server server to create
	 *
	 * @return Response will be the server just created in or an error code with a
	 *         message corresponding to the exception caught.
	 */
	@Authorize
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response newServer(Server server) {
		try {
			Server newServer = serverService.createServer(server);
			return Response.status(Response.Status.CREATED).entity(newServer).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		}
	}

	/**
	 * Method handling HTTP PUT requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 * 
	 * @param server server to update
	 * @param alias  alias of the server to update
	 * 
	 * @return Response will be the server updated or an error code with a message
	 *         corresponding to the exception caught.
	 */
	@Authorize
	@PUT
	@Path("{alias}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateServer(@PathParam("alias") String alias, Server server) {
		try {
			Server updatedServer = serverService.updateServer(alias, server);
			return Response.ok().entity(updatedServer).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		}
	}

	/**
	 * Method handling HTTP DELETE requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 * 
	 * @param alias server to delete
	 *
	 * @return Response will be 204 no content if succeed or an error code with a
	 *         message corresponding to the exception caught.
	 */
	@Authorize
	@DELETE
	@Path("{alias}")
	public Response deleteServer(@PathParam("alias") String alias) {
		try {
			serverService.deleteServer(alias);
			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		}
	}

}
