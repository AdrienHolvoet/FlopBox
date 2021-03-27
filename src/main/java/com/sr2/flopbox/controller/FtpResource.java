package com.sr2.flopbox.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sr2.flopbox.common.Command;
import com.sr2.flopbox.exception.HandleException;
import com.sr2.flopbox.service.FtpService;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Ftp resource (exposed at "{alias}" path) which allows anyone to access the
 * ftp server registered on the flopBox platform and to perform operations
 * specific to the ftp protocol
 *
 * @author Adrien Holvoet
 */

@Path("/{alias}")
public class FtpResource {

	// private because it is only used in this class
	private static final Logger logger = LogManager.getLogger(FtpResource.class);

	/**
	 * Method handling HTTP get requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 * 
	 * @param alias         the ftp server
	 * @param path          path of the repository
	 * @param mode          set passive to enter in passive mode, All other values
	 *                      let active
	 * @param authorization HTTP request header authorization value
	 *
	 * @return Response will be the list of files in the specified folder (path) or
	 *         an error code with a message corresponding to the exception caught.
	 *
	 */
	@GET
	@Path("/list/{path: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response GetList(@PathParam("alias") String alias, @PathParam("path") String path,
			@QueryParam("mode") String mode, @HeaderParam("Authorization") String authorization) {

		FtpService ftpService = new FtpService(alias, authorization, Command.LIST, path, null, mode);

		try {
			synchronized (ftpService) {
				ftpService.start();
				ftpService.wait();
			}

			if (ftpService.getException() != null) {
				// It means that something went wrong in the thread, need to notice the client
				throw ftpService.getException();
			}

			return Response.ok(ftpService.getResultObject()).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		} finally {
			// Disconnect server
			try {
				ftpService.logout();
			} catch (IOException e) {
				// Do nothing server already close
			}
		}
	}

	/**
	 * Method handling HTTP get requests. The returned object will be sent to the
	 * client as "application/octet_stream" media type.
	 * 
	 * @param alias         the ftp server
	 * @param path          path of the file
	 * @param mode          set passive to enter in passive mode, All other values
	 *                      let active
	 * @param authorization HTTP request header authorization value
	 *
	 * @return Response will be the download of the specified file (path) or an
	 *         error code with a message corresponding to the exception caught.
	 *
	 */
	@GET
	@Path("files/{path: .*}")
	public Response getFile(@PathParam("alias") String alias, @PathParam("path") String path,
			@QueryParam("mode") String mode, @HeaderParam("Authorization") String authorization) {

		FtpService ftpService = new FtpService(alias, authorization, Command.GETF, path, null, mode);

		try {
			synchronized (ftpService) {
				ftpService.start();

				ftpService.wait();
			}

			if (ftpService.getException() != null) {
				// It means that something went wrong in the thread, need to notice the client
				throw ftpService.getException();
			}

			return Response.ok(ftpService.getResultObject(), MediaType.APPLICATION_OCTET_STREAM).build();

		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		} finally {
			// Disconnect server
			try {
				ftpService.logout();
			} catch (IOException e) {
				// Do nothing server already close
			}
		}
	}

	/**
	 * Method handling HTTP get requests. The returned object will be sent to the
	 * client as "application/octet_stream" media type.
	 * 
	 * @param alias         the ftp server
	 * @param path          path of the remote repository
	 * @param mode          set passive to enter in passive mode, All other values
	 *                      let active
	 * @param authorization HTTP request header authorization value
	 *
	 * @return Response will be the 204 no content or an error code with a message
	 *         corresponding to the exception caught.
	 *
	 */
	@GET
	@Path("repositories/{path: .*}")
	public Response getRepository(@PathParam("alias") String alias, @PathParam("path") String path,
			@QueryParam("downloadFolder") String downloadFolder, @QueryParam("mode") String mode,
			@HeaderParam("Authorization") String authorization) {

		FtpService ftpService = new FtpService(alias, authorization, Command.GETD, path, downloadFolder, mode);

		try {

			synchronized (ftpService) {
				ftpService.start();
				ftpService.wait();
			}

			if (ftpService.getException() != null) {
				// It means that something went wrong in the thread, need to notice the client
				throw ftpService.getException();
			}
			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		} finally {
			// Disconnect server
			try {
				ftpService.logout();
			} catch (IOException e) {
				// Do nothing server already close
			}
		}
	}

	/**
	 * Method handling HTTP PUT requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 * 
	 * @param alias         the ftp server
	 * @param path          path of the remote repository
	 * @param localPath     path of the file or repository to store
	 * @param mode          set passive to enter in passive mode, All other values
	 *                      let active
	 * @param authorization HTTP request header authorization value
	 *
	 * @return Response will be 204 no content or an error code with a message
	 *         corresponding to the exception caught.
	 *
	 */
	@PUT
	@Path("/{path: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFile(@PathParam("alias") String alias, @PathParam("path") String path,
			@QueryParam("localPath") String localPath, @QueryParam("mode") String mode,
			@HeaderParam("Authorization") String authorization) {

		FtpService ftpService = new FtpService(alias, authorization, Command.PUT, path, localPath, mode);

		try {

			synchronized (ftpService) {
				ftpService.start();
				ftpService.wait();
			}

			if (ftpService.getException() != null) {
				// It means that something went wrong in the thread, need to notice the client
				throw ftpService.getException();
			}

			return Response.status(Response.Status.NO_CONTENT).build();

		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		} finally {
			// Disconnect session
			try {
				ftpService.logout();
			} catch (IOException e) {
				// Do nothing server already close
			}
		}
	}

	/**
	 * Method handling HTTP PUT requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 * 
	 * @param alias         the ftp server
	 * @param path          path of the remote repository/file
	 * @param name          new name of the remote repository/file
	 * @param mode          set passive to enter in passive mode, All other values
	 *                      let active
	 * @param authorization HTTP request header authorization value
	 *
	 * @return Response will be the new path of the the remote repository/file or an
	 *         error code with a message corresponding to the exception caught.
	 *
	 */
	@PUT
	@Path("rename/{path: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response renameFIle(@PathParam("alias") String alias, @PathParam("path") String path,
			@QueryParam("name") String name, @QueryParam("mode") String mode,
			@HeaderParam("Authorization") String authorization) {

		FtpService ftpService = new FtpService(alias, authorization, Command.REN, path, name, mode);

		try {
			synchronized (ftpService) {
				ftpService.start();
				ftpService.wait();
			}

			if (ftpService.getException() != null) {
				// It means that something went wrong in the thread, need to notice the client
				throw ftpService.getException();
			}
			return Response.ok(ftpService.getResultObject()).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		} finally {
			// Disconnect session
			try {
				ftpService.logout();
			} catch (IOException e) {
				// Do nothing server already close
			}
		}
	}

	/**
	 * Method handling HTTP POST requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 * 
	 * @param alias         the ftp server
	 * @param path          path of the new repository
	 * @param mode          set passive to enter in passive mode, All other values
	 *                      let active
	 * @param authorization HTTP request header authorization value
	 *
	 * @return Response will be the path of the newly created directory or an error
	 *         code with a message corresponding to the exception caught.
	 *
	 */
	@POST
	@Path("repositories/{path: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createFile(@PathParam("alias") String alias, @PathParam("path") String path,
			@QueryParam("mode") String mode, @HeaderParam("Authorization") String authorization) {

		FtpService ftpService = new FtpService(alias, authorization, Command.MKD, path, null, mode);

		try {

			synchronized (ftpService) {
				ftpService.start();
				ftpService.wait();
			}

			if (ftpService.getException() != null) {
				// It means that something went wrong in the thread, need to notice the client
				throw ftpService.getException();
			}

			return Response.ok(ftpService.getResultObject()).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		} finally {
			// Disconnect session
			try {
				ftpService.logout();
			} catch (IOException e) {
				// Do nothing server already close
			}
		}
	}

	/**
	 * Method handling HTTP DELETE requests. The returned object will be sent to the
	 * client as "application/json" media type.
	 * 
	 * @param alias         the ftp server
	 * @param path          path of the repository to delete
	 * @param authorization HTTP request header authorization value
	 *
	 * @return Response will be 204 or an error code with a message
	 *         corresponding to the exception caught.
	 */
	@DELETE
	@Path("repositories/{path: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRepository(@PathParam("alias") String alias, @PathParam("path") String path,
			@QueryParam("mode") String mode, @HeaderParam("Authorization") String authorization) {

		FtpService ftpService = new FtpService(alias, authorization, Command.RMD, path, null, mode);

		try {

			synchronized (ftpService) {
				ftpService.start();
				ftpService.wait();
			}

			if (ftpService.getException() != null) {
				// It means that something went wrong in the thread, need to notice the client
				throw ftpService.getException();
			}

			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (Exception e) {
			return HandleException.handleException(e, logger);
		} finally {
			// Disconnect session
			try {
				ftpService.logout();
			} catch (IOException e) {
				// Do nothing server already close
			}
		}
	}
}
