package com.sr2.flopbox.controllerTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;

import com.sr2.flopbox.service.ServerService;
import org.glassfish.grizzly.http.server.HttpServer;

import com.sr2.flopbox.Main;
import com.sr2.flopbox.common.Constant;
import com.sr2.flopbox.exception.NotAuthorizedException;
import com.sr2.flopbox.model.Credentials;
import com.sr2.flopbox.model.JwtToken;
import com.sr2.flopbox.model.Server;
import com.sr2.flopbox.service.AuthenticationService;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
public class ServerResourceTest {

    private static HttpServer server;
    private static WebTarget target;
    private static String resourceName;
    private static final AuthenticationService authenticationService = AuthenticationService.getInstance();
    private static String alias = "ubuntu";
    private static final String aliasUpdated = "ubuntuUpdated";
    private static ServerService serverService = ServerService.getInstance();

    @BeforeAll
    public static void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        // create the client
        resourceName = "servers";
        Client c = ClientBuilder.newClient();

        target = c.target(Constant.BASE_URI + resourceName);
        serverService.createServer(new Server("test","ftp.test.com",21));
    }

    @AfterAll
    public static void tearDown() throws Exception {
        server.stop();
        serverService.deleteServer(aliasUpdated);
    }
    @Order(1)
    @Test
    public void newServerPOST_shouldReturn201AndCreatedServer_WhenConnectedAndGoodEntityIsUsed()
            throws FileNotFoundException, NotAuthorizedException {
        Server newServer = new Server("ubuntu", "ftp.ubuntu.com", 21);
        JwtToken jwtToken = authenticationService.authenticate(new Credentials("root", "root"));

        Response response = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken.getToken()).post(Entity.json(newServer));
        String entity = response.readEntity(String.class);

        assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals("{\"address\":\"ftp.ubuntu.com\",\"alias\":\"ubuntu\",\"port\":21}", entity);
    }
    @Order(2)
    @Test
    public void newServerPOST_shouldReturn401AndErrorResponse_WhenNotConnected()
            throws FileNotFoundException, NotAuthorizedException {
        Server newServer = new Server(alias, "ftp.ubuntu.com", 21);

        Response response = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(newServer));
        String entity = response.readEntity(String.class);

        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"WWW-Authenticate=Bearer\",\"statusCode\":401}", entity);
    }
    @Order(3)
    @Test
    public void getServersGET_shouldGetAllServersAnd200Code_allOfThem() {
        Response response = target.request().get();
        String entities = response.readEntity(String.class);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(entities);
    }
    @Order(4)
    @Test
    public void getServerGET_shouldGetSpecificServerAnd200Code_KnowServer() {
        Response response = target.path(alias).request().get();
        String entity = response.readEntity(String.class);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"address\":\"ftp.ubuntu.com\",\"alias\":\"ubuntu\",\"port\":21}", entity);
    }
    @Order(5)
    @Test
    public void getServerGET_shouldGetNullServerAnd404Code_UnknowServer() {
        Response response = target.path("UNKNOWSTUFF").request().get();
        String entity = response.readEntity(String.class);

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(
                "{\"message\":\"The server requested doesn't exist on the FlopBox application\",\"statusCode\":404}",
                entity);
    }

    @Order(6)
    @Test
    public void updateServerPUT_shouldReturn401AndErrorResponse_WhenNotConnected() {
        Server ServerUpdated = new Server(aliasUpdated, "ftp.ubuntu.com", 5000);

        Response response = target.path(alias).request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .put(Entity.json(ServerUpdated));
        String entity = response.readEntity(String.class);

        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"WWW-Authenticate=Bearer\",\"statusCode\":401}", entity);
    }

    @Order(7)
    @Test
    public void updateServerPUT_shouldGetNullServerAnd404Code_whenConnectedAndUnknowServer()
            throws FileNotFoundException, NotAuthorizedException {
        Server ServerUpdated = new Server(aliasUpdated, "ftp.ubuntu.com", 5000);
        JwtToken jwtToken = authenticationService.authenticate(new Credentials("root", "root"));

        Response response = target.path("UNKNOWSTUFF").request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken.getToken())
                .put(Entity.json(ServerUpdated));
        String entity = response.readEntity(String.class);

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(
                "{\"message\":\"The server requested doesn't exist on the FlopBox application\",\"statusCode\":404}",
                entity);
    }

    @Order(8)
    @Test
    public void updateServerPUT_shouldGetUpdatedServerAnd200Code_WhenKnowServerAndConnected()
            throws FileNotFoundException, NotAuthorizedException {
        Server ServerUpdated = new Server(aliasUpdated, "ftp.ubuntu.com", 5000);
        JwtToken jwtToken = authenticationService.authenticate(new Credentials("root", "root"));

        Response response = target.path("test").request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken.getToken()).put(Entity.json(ServerUpdated));
        String entity = response.readEntity(String.class);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"address\":\"ftp.ubuntu.com\",\"alias\":\"ubuntuUpdated\",\"port\":5000}", entity);
    }

    @Order(9)
    @Test
    public void deleteServerDELETE_shouldReturn401AndErrorResponse_WhenNotConnected() {

        Response response = target.path(alias).request().delete();
        String entity = response.readEntity(String.class);

        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"WWW-Authenticate=Bearer\",\"statusCode\":401}", entity);
    }

    @Order(10)
    @Test
    public void deleteServerDELETE_shouldReturn404CodeAndErrorResponse_whenConnectedAndUnknowServer()
            throws FileNotFoundException, NotAuthorizedException {
        JwtToken jwtToken = authenticationService.authenticate(new Credentials("root", "root"));

        Response response = target.path("UNKNOWSTUFF").request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken.getToken()).delete();

        String entity = response.readEntity(String.class);

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(
                "{\"message\":\"The server requested doesn't exist on the FlopBox application\",\"statusCode\":404}",
                entity);
    }

    @Test
    @Order(11)
    public void deleteServerDELETE_shouldGet201Code_WhenknowServerAndConnected()
            throws FileNotFoundException, NotAuthorizedException, InterruptedException {
        JwtToken jwtToken = authenticationService.authenticate(new Credentials("root", "root"));

        Response response = target.path(alias).request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken.getToken()).delete();

        String entity = response.readEntity(String.class);

        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertEquals("", entity);

    }
}
