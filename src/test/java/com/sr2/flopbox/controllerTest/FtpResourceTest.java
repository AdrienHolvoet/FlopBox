package com.sr2.flopbox.controllerTest;

import com.sr2.flopbox.Main;
import com.sr2.flopbox.common.Constant;
import com.sr2.flopbox.model.Server;
import com.sr2.flopbox.service.ServerService;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static org.junit.Assert.*;

public class FtpResourceTest {
    // static because same instance for all
    private static HttpServer server;
    private static WebTarget target;
    private static ServerService serverService = ServerService.getInstance();
    private static String alias = "ubuntu";
    private static final String TMP_DIR = "src/main/resources/tmp_dir";
    private static final String TMP_FILE_ONE = "src/main/resources/tmp_file";

    @BeforeClass
    public static void setUp() throws Exception {
        // start the server
        server = Main.startServer();

        // create the client
        Client c = ClientBuilder.newClient();
        target = c.target(Constant.BASE_URI);
        serverService.createServer(new Server(alias, "ftp.ubuntu.com", 21));

        Files.createDirectory(Paths.get(TMP_DIR));
        Files.createFile(Paths.get(TMP_FILE_ONE));

    }

    @AfterClass
    public static void tearDown() throws Exception {
        serverService.deleteServer(alias);
        server.stop();
        FileUtils.deleteDirectory(new File(TMP_DIR));
        Files.deleteIfExists(Paths.get(TMP_FILE_ONE));
    }

    @Test
    public void everyCall_shouldGet401CodeAndOnlyAnonymous_whenUsingBasicWithOtherThanAnonymous() {
        String credential = "wrong:wrong";
        String wrong = Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));

        Response responseGetList = target.path(alias + "/list/").queryParam("mode", "passive").request().header(HttpHeaders.AUTHORIZATION, "Basic " + wrong).get();
        String entityGetList = responseGetList.readEntity(String.class);
        Response responsePut = target.path(alias + "/").queryParam("localPath", TMP_FILE_ONE).request().header(HttpHeaders.AUTHORIZATION, "Basic " + wrong).put(Entity.json("null"));
        String entityPut = responsePut.readEntity(String.class);
        Response responseGetRepo = target.path(alias + "/repositories/").queryParam("downloadFolder", TMP_DIR).queryParam("mode", "passive").request().header(HttpHeaders.AUTHORIZATION, "Basic " + wrong).get();
        String entityGetRepo = responseGetRepo.readEntity(String.class);
        Response responseGetFile = target.path(alias + "/files/WrongPath").request().header(HttpHeaders.AUTHORIZATION, "Basic " + wrong).get();
        String entityGetFile = responseGetFile.readEntity(String.class);
        Response responsePost = target.path(alias + "/repositories/").request().header(HttpHeaders.AUTHORIZATION, "Basic " + wrong).post(Entity.json("null"));
        String entityPost = responsePost.readEntity(String.class);
        Response responseDelete = target.path(alias + "/repositories/wrong").request().header(HttpHeaders.AUTHORIZATION, "Basic " + wrong).delete();
        String entityDelete = responseDelete.readEntity(String.class);
        Response responseRename = target.path(alias + "/rename/ubuntu").queryParam("name", "newName").request().header(HttpHeaders.AUTHORIZATION, "Basic " + wrong).put(Entity.json("null"));
        String entityRename = responseRename.readEntity(String.class);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responseGetList.getStatus());
        assertEquals("{\"message\":\"FTP : 530 This FTP server is anonymous only.\",\"statusCode\":401}", entityGetList);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responsePut.getStatus());
        assertEquals("{\"message\":\"FTP : 530 This FTP server is anonymous only.\",\"statusCode\":401}", entityPut);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responseGetRepo.getStatus());
        assertEquals("{\"message\":\"FTP : 530 This FTP server is anonymous only.\",\"statusCode\":401}", entityGetRepo);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responseGetFile.getStatus());
        assertEquals("{\"message\":\"FTP : 530 This FTP server is anonymous only.\",\"statusCode\":401}", entityGetFile);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responsePost.getStatus());
        assertEquals("{\"message\":\"FTP : 530 This FTP server is anonymous only.\",\"statusCode\":401}", entityPost);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responseDelete.getStatus());
        assertEquals("{\"message\":\"FTP : 530 This FTP server is anonymous only.\",\"statusCode\":401}", entityDelete);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responseRename.getStatus());
        assertEquals("{\"message\":\"FTP : 530 This FTP server is anonymous only.\",\"statusCode\":401}", entityRename);
    }

    @Test
    public void getListGET_shouldGetEmptyDirAnd200Code_whenActiveMode() {
        Response response = target.path(alias + "/list/").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void getListGET_shouldGetDirAnd200Code_whenPassiveMode() {
        Response response = target.path(alias + "/list/").queryParam("mode", "passive").request().get();
        String entities = response.readEntity(String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(entities);
    }

    @Test
    public void getListGET_shouldGet200AndList_whenGoodCredentialsAndPassiveMode() {
        String credential = "anonymous:anonymous";
        String wrong = Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));

        Response response = target.path(alias + "/list/").queryParam("mode", "passive").request().header(HttpHeaders.AUTHORIZATION, "Basic " + wrong).get();
        String entities = response.readEntity(String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(entities);
    }

    @Test
    public void getFileGET_shouldGet404_whenFileDoesNotExist() {
        Response response = target.path(alias + "/files/WrongPath").request().get();
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"One of the specified path doesn't exist\",\"statusCode\":404}", entity);
    }

    @Test
    public void getRepositoryGET_shouldGet404_whenRepoDoesNotExist() {
        Response response = target.path(alias + "/repositories/WrongPath").request().get();
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"One of the specified path doesn't exist\",\"statusCode\":404}", entity);
    }

    @Test
    public void getRepositoryGET_shouldGet204_whenRepoDoesExist() {
        String repoToDownload = "/ubuntu/pool/universe/0/0xffff";
        Response response = target.path(alias + "/repositories" + repoToDownload).queryParam("downloadFolder", TMP_DIR).queryParam("mode", "passive").request().get();

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertTrue(new File(TMP_DIR + repoToDownload).exists());
    }

    @Test
    public void uploadFilePUT_shouldGet404_whenPathAreWrong() {
        Response response = target.path(alias + "/").queryParam("localPath", "wrong").request().put(Entity.json("null"));
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"One of the specified path doesn't exist\",\"statusCode\":404}", entity);
    }

    @Test
    public void uploadFilePUT_shouldGet403AndIllegalPortMessage_whenUploadFileInActiveMode() {
        Response response = target.path(alias + "/").queryParam("localPath", TMP_FILE_ONE).request().put(Entity.json("null"));
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"FTP : 550 Permission denied. You don't have enough Permission\",\"statusCode\":403}", entity);
    }

    @Test
    public void uploadFilePut_shouldGet403AndPermissionDenied_whenUploadFileInPassiveMode() {
        Response response = target.path(alias + "/").queryParam("localPath", TMP_FILE_ONE).queryParam("mode", "passive").request().put(Entity.json("null"));
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"FTP : 550 Permission denied. You don't have enough Permission\",\"statusCode\":403}", entity);
    }

    @Test
    public void createRepositoryPOST_shouldGet403AndPermissionDenied_whenTryingToCreate() {
        Response response = target.path(alias + "/repositories/").request().post(Entity.json("null"));
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"FTP : 550 Permission denied. You don't have enough Permission\",\"statusCode\":403}", entity);
    }

    @Test
    public void deleteRepositoryDELETE_shouldGet403AndPermissionDenied_whenDeletingHomeRepo() {
        Response response = target.path(alias + "/repositories/").request().delete();
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"You cannot delete the home repository You don't have enough Permission\",\"statusCode\":403}", entity);
    }

    @Test
    public void deleteRepositoryDELETE_shouldGet403AndPermissionDenied_whenDeletingGoodPath() {
        Response response = target.path(alias + "/repositories/ubuntu").request().delete();
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"FTP : 550 Permission denied. You don't have enough Permission\",\"statusCode\":403}", entity);
    }

    @Test
    public void deleteRepositoryDELETE_shouldGet404_whenPathIsWrong() {
        Response response = target.path(alias + "/repositories/wrong").request().delete();
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"The specified path doesn't exist\",\"statusCode\":404}", entity);
    }

    @Test
    public void renameRepositoryPUT_shouldGet404_whenPathIsWrong() {
        Response response = target.path(alias + "/rename/wrongPath").queryParam("name", "newName").request().put(Entity.json("null"));
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"The specified path doesn't exist\",\"statusCode\":404}", entity);
    }

    @Test
    public void renameRepositoryPUT_shouldGet403AndPermissionDenied_whenDeletingGoodPath() {
        Response response = target.path(alias + "/rename/ubuntu").queryParam("name", "newName").request().put(Entity.json("null"));
        String entity = response.readEntity(String.class);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("{\"message\":\"FTP : 550 Permission denied. You don't have enough Permission\",\"statusCode\":403}", entity);
    }

}
