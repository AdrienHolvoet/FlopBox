package com.sr2.flopbox.service;

import com.sr2.flopbox.common.CheckUtils;
import com.sr2.flopbox.common.Command;
import com.sr2.flopbox.common.Constant;
import com.sr2.flopbox.exception.NotAuthorizedException;
import com.sr2.flopbox.exception.UnexpectedFtpStatusCodeException;
import com.sr2.flopbox.model.Server;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Base64;

/**
 * Class which contains all the logic linked to the ftp resource. This service
 * will operate all the commands related to a server ftp. Implements Thread to
 * allow multiple simultaneous connections to ftp servers.
 *
 * @author Adrien Holvoet
 */
public class FtpService extends Thread {
    /*
     * All private because they are only used in this class
     */
    private FTPClient ftpClient;
    private ServerService serverService = ServerService.getInstance();
    private Object resultObject = null;
    private String alias;
    private String authorization;
    private Command command;
    private String args0;
    private String args1;
    private String mode;
    private Exception exception;

    /**
     * Constructor used to get all the data from the resources and use the
     * run(Thread)
     *
     * @param alias         the ftp server
     * @param command       the command executed
     * @param authorization HTTP request header authorization value
     */
    public FtpService(String alias, String authorization, Command command, String args0, String args1, String mode) {
        super();
        this.alias = alias;
        this.authorization = authorization;
        this.command = command;
        this.args0 = args0;
        this.args1 = args1;
        this.mode = mode;
    }

    /**
     * The method need to be implemented from Thread class. This is the method
     * launch when the thread is start. This méthod will just run the executeCommand
     * method. And save the exception raised if one.
     */
    public void run() {
        try {
            this.executeCommand(alias, authorization, command, args0, args1, mode);
        } catch (Exception e) {
            // catch all exceptions thrown to notify clients
            this.exception = e;
        }
    }

    /**
     * Execute the commands received by the FtpResource
     *
     * @param args        Corresponding to the local/remote file/repository
     * @param alias       Server ftp used
     * @param credentials Corresponding to the user
     * @param command     The command requested
     * @param mode        Mode passive or active
     * @throws IOException                      thrown if any I/O error occurred.
     * @throws UnexpectedFtpStatusCodeException
     * @throws NotFoundException
     * @throws NotAuthorizedException
     */
    public void executeCommand(String alias, String credentials, Command command, String args0, String args1,
                               String mode) throws IOException, UnexpectedFtpStatusCodeException, NotAuthorizedException {

        this.connect(alias);

        // switch passive mode if the query param = passive, let the default active mode
        // otherwise
        if (mode != null && mode.equals(Constant.PASSIVE)) {
            this.passiveMode();
        }

        this.login(credentials);


        args0 = File.separator + args0;
        switch (command) {

            case LIST:
                resultObject = this.list(args0);
                break;
            case GETF:
                if ((checkFileExists(args0))) {
                    resultObject = this.getF(args0);
                } else {
                    throw new NotFoundException("One of the specified path doesn't exist");
                }
                break;
            case GETD:
                if (checkDirectoryExists(args0) && CheckUtils.checkIfLocalRepositoryExist(args1)) {
                    this.getD(args0, "", args1);
                } else {
                    throw new NotFoundException("One of the specified path doesn't exist");
                }
                break;
            case PUT:
                if (args1 == null) {
                    throw new BadRequestException("Query param cannot be null");
                }
                File file = new File(args1);
                if ((checkDirectoryExists(args0)) && CheckUtils.checkIfLocalFileExist(file)) {
                    resultObject = this.putF(args0, args1);
                } else if (checkDirectoryExists(args0) && file.exists()) {
                    String dir = new File(args0).getAbsolutePath();
                    this.ftpClient.makeDirectory(dir + File.separator + file.getName());
                    this.putD(dir + File.separator + file.getName(), args1, "");
                } else {
                    throw new NotFoundException("One of the specified path doesn't exist");
                }
                break;
            case REN:
                resultObject = this.rename(args0, args1);
                break;
            case MKD:
                resultObject = this.createRepository(args0);
                break;
            case RMD:
                this.deleteDirectory(args0);
                break;
            default:
                break;
        }
        Thread.currentThread().interrupt();
    }

    /**
     * Connects to a remote FTP server
     *
     * @param alias remote ServerFtp
     * @throws IOException                      Thrown if any I/O error occurred.
     * @throws UnexpectedFtpStatusCodeException If the server doesn't answer by the
     *                                          expected code
     * @throws NotFoundException                If the sever is not found
     */
    private void connect(String alias) throws IOException, UnexpectedFtpStatusCodeException {
        Server server = serverService.getServer(alias);
        this.ftpClient = new FTPClient();
        try {
            this.ftpClient.connect(server.getAddress(), server.getPort());
        } catch (IOException e) {
            throw new NotFoundException(
                    String.format("Unable to connect to the following ftp server %s:%s. Check if it is connected",
                            server.getAddress(), server.getPort()));
        }
        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            throw new UnexpectedFtpStatusCodeException(
                    String.format("The ftp server did not respond with the correct status code %d, %s",
                            ftpClient.getReplyCode(), ftpClient.getReplyString()));
        }

    }

    /**
     * Switch the connection to passive mode
     *
     * @throws NotAllowedException if something went wrong
     */
    private void passiveMode() {
        this.ftpClient.enterLocalPassiveMode();
        if (!FTPReply.isPositiveCompletion(this.ftpClient.getReplyCode())) {
            throw new NotAllowedException("Cannot switch to passive mode, FTP : " + this.ftpClient.getReplyString());
        }

    }

    /**
     * Login the user on the remote ftp server
     *
     * @param credentials basic authorization token
     * @throws NotAuthorizedException If the connection is not successful
     */
    private void login(String credentials) throws NotAuthorizedException {

        try {
            if (!CheckUtils.checkIfStringIsNull(credentials)) {
                this.ftpClient.login(Constant.ANONYMOUS, Constant.ANONYMOUS);
            } else {
                if (!credentials.startsWith("Basic")) {
                    throw new NotAuthorizedException("WWW-Authenticate=Basic");
                }
                String[] tokens = (new String(Base64.getDecoder().decode(credentials.split(" ")[1]), "UTF-8"))
                        .split(":");

                final String username = tokens[0];
                final String password = tokens[1];
                this.ftpClient.login(username, password);
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            throw new NotAuthorizedException("The credentials are wrong");
        }

        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            throw new NotAuthorizedException("FTP : " + ftpClient.getReplyString());
        }
    }

    /**
     * List the directory specified in parameter
     *
     * @param dirPath directory path
     * @return FTPFile[] all files with their metadata
     * @throws IOException       thrown if any I/O error occurred.
     * @throws NotFoundException if the directory does not exist
     */
    private FTPFile[] list(String dirPath) throws IOException {
        if (!(checkDirectoryExists(dirPath))) {
            throw new NotFoundException("This repository doesn't exist");
        }
        FTPFile[] list = null;
        list = this.ftpClient.listFiles(dirPath);
        return list;
    }

    /**
     * Retrieve the file specified in parameter
     *
     * @param filePath Path of the file downloaded
     * @return inputStream The specific file
     * @throws IOException Thrown if any I/O error occurred.
     */
    private InputStream getF(String filePath) throws IOException {
        // Change transfer file type
        if (CheckUtils.isImage(new File(filePath))) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        }

        return ftpClient.retrieveFileStream(filePath);
    }

    /**
     * Retrieve the file specified in parameter in the specified folder, used only
     * for the recursion in getD
     *
     * @param filePath     path of the file on the server  
     * @param downloadPath path of directory where the file will be stored
     * @return true if success, false otherwise
     */
    private boolean downloadFileInGetD(String filePath, String downloadPath) throws IOException {
        File downloadFile = new File(downloadPath);

        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
        try {
            if (CheckUtils.isImage(new File(filePath))) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            } else {
                ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
            }
            return ftpClient.retrieveFile(filePath, outputStream);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * Download a whole directory from a FTP server.
     *
     * @param remoteFolderPath path of the main folder which is download
     * @param currentDir       path of the current directory being downloaded.
     * @param downloadFolder   path of directory where the file will be stored
     * @return true if success, false otherwise
     * @throws IOException thrown if any I/O error occurred.
     */
    private void getD(String remoteFolderPath, String currentDir, String downloadFolder) throws IOException {
        String dirToList = remoteFolderPath;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = remoteFolderPath + "/" + currentDir + "/" + currentFileName;
                if (currentDir.equals("")) {
                    filePath = remoteFolderPath + "/" + currentFileName;
                }

                String newDirPath = downloadFolder + remoteFolderPath + File.separator + currentDir + File.separator
                        + currentFileName;
                if (currentDir.equals("")) {
                    newDirPath = downloadFolder + remoteFolderPath + File.separator + currentFileName;
                }

                if (aFile.isDirectory()) {
                    // create the directory in saveDir
                    File newDir = new File(newDirPath);
                    newDir.mkdirs();

                    // download the sub directory
                    getD(dirToList, currentFileName, downloadFolder);
                } else {
                    // download the file
                    downloadFileInGetD(filePath, newDirPath);
                }
            }
        }

    }

    /**
     * Upload a file
     *
     * @param filePath file to store
     * @param dirPath  remote folder where to store the file
     * @return true if store
     * @throws IOException         thrown if any I/O error occurred.
     * @throws NotFoundException   if the resource is not found
     * @throws ForbiddenException  if the user don't have enough access
     * @throws BadRequestException if filePath is null
     */
    private boolean putF(String dirPath, String filePath) throws IOException {
        if (!CheckUtils.checkIfStringIsNull(filePath)) {
            throw new BadRequestException("Query Param cannot be null");
        }
        File file = new File(filePath);
        if (!(CheckUtils.checkIfLocalFileExist(file))) {
            throw new NotFoundException("The local file doesn't exist");
        }
        this.ftpClient.changeWorkingDirectory(dirPath);

        FileInputStream in = new FileInputStream(file);

        // Change transfer file type
        if (CheckUtils.isImage(file)) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } else {
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
        }
        boolean state = this.ftpClient.storeFile(file.getName(), in);

        in.close();
        if (!FTPReply.isPositiveCompletion(this.ftpClient.getReplyCode())) {
            throw new ForbiddenException("FTP : " + this.ftpClient.getReplyString());
        }
        return state;
    }

    /**
     * Upload a whole directory to a FTP server.
     *
     * @param remoteDirPath   Path of the destination directory on the server.
     * @param localParentDir  Path of the local directory being uploaded.
     * @param remoteParentDir Path of the parent directory of the current directory
     *                        on the server (used by recursive calls).
     * @throws IOException if any network or IO error occurred.
     */
    public void putD(String remoteDirPath, String localParentDir, String remoteParentDir) throws IOException {
        File localDir = new File(localParentDir);
        File[] subFiles = localDir.listFiles();

        if (subFiles != null && subFiles.length > 0) {
            for (File item : subFiles) {
                String remoteFilePath = remoteDirPath + "/" + remoteParentDir + "/" + item.getName();
                if (remoteParentDir.equals("")) {
                    remoteFilePath = remoteDirPath + "/" + item.getName();
                }

                if (item.isFile()) {
                    // upload the file
                    String localFilePath = item.getAbsolutePath();
                    putF(remoteFilePath.replace(item.getName(), ""), localFilePath);
                } else {
                    // create directory on the server
                    ftpClient.makeDirectory(remoteFilePath);

                    // upload the sub directory
                    String parent = remoteParentDir + "/" + item.getName();
                    if (remoteParentDir.equals("")) {
                        parent = item.getName();
                    }
                    localParentDir = item.getAbsolutePath();
                    putD(remoteDirPath, localParentDir, parent);
                }
            }
        }
    }

    /**
     * Rename a file/directory
     *
     * @param path path of the remote repository/file
     * @param name new name of the remote repository/file
     * @return newName if renamed
     * @throws IOException         thrown if any I/O error occurred.
     * @throws NotFoundException   if the resource is not found
     * @throws ForbiddenException  if the user don't have enough access
     * @throws BadRequestException if name is null
     */
    private String rename(String path, String name) throws IOException {
        if (!(checkFileExists(path)) && !(checkDirectoryExists(path))) {
            throw new NotFoundException("The specified path doesn't exist");
        }

        if (!CheckUtils.checkIfStringIsNull(name)) {
            throw new BadRequestException("Query Param cannot be null");
        }
        String[] arrOfStr = path.split(File.separator);
        String newName = File.separator;
        for (int i = 1; i < arrOfStr.length - 1; i++) {
            newName += arrOfStr[i] + File.separator;
        }

        newName = newName + name;
        ftpClient.rename(path, newName);

        if (!FTPReply.isPositiveCompletion(this.ftpClient.getReplyCode())) {
            throw new ForbiddenException("FTP : " + this.ftpClient.getReplyString());
        }
        return newName;
    }

    /**
     * Create a directory
     *
     * @param path path of the new repository
     * @return path if created
     * @throws IOException         thrown if any I/O error occurred.
     * @throws ForbiddenException  if the user don't have enough access
     * @throws BadRequestException if path is null
     */
    private String createRepository(String path) throws IOException {
        if (!CheckUtils.checkIfStringIsNull(path)) {
            throw new BadRequestException("The param cannot be null");
        }
        ftpClient.makeDirectory(path);

        if (!FTPReply.isPositiveCompletion(this.ftpClient.getReplyCode())) {
            throw new ForbiddenException("FTP : " + this.ftpClient.getReplyString());
        }
        return path;
    }

    /**
     * Delete a directory/file
     *
     * @param path path of file/repo to delete
     * @return true if deleted
     * @throws IOException         thrown if any I/O error occurred.
     * @throws ForbiddenException  if the user don't have enough access
     * @throws BadRequestException if path is null
     */
    private boolean deleteDirectory(String path) throws IOException {
        if (!(checkFileExists(path)) && !(checkDirectoryExists(path))) {
            throw new NotFoundException("The specified path doesn't exist");
        }

        if (path.equals(File.separator)) {
            throw new ForbiddenException("You cannot delete the home repository");
        }

        FTPFile[] subFiles = ftpClient.listFiles(path);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = path + "/" + currentFileName;

                if (aFile.isDirectory()) {
                    // remove the sub directory
                    deleteDirectory(filePath);
                } else {
                    // delete the file
                    ftpClient.deleteFile(filePath);
                }
            }
        }
        // finally, remove the directory itself
        boolean removed = ftpClient.removeDirectory(path);

        if (!FTPReply.isPositiveCompletion(this.ftpClient.getReplyCode())) {
            throw new ForbiddenException("FTP : " + this.ftpClient.getReplyString());
        }

        return removed;
    }

    /**
     * Determines whether a directory exists or not
     *
     * @param dirPath
     * @return true if exists, false otherwise
     * @throws IOException thrown if any I/O error occurred.
     */

    private boolean checkDirectoryExists(String dirPath) throws IOException {
        ftpClient.cwd(dirPath);
        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether a file exists or not
     *
     * @param filePath
     * @return true if exists, false otherwise
     * @throws IOException thrown if any I/O error occurred.
     */
    private boolean checkFileExists(String filePath) throws IOException {
        InputStream inputStream = ftpClient.retrieveFileStream(filePath);
        int returnCode = ftpClient.getReplyCode();
        if (inputStream == null || returnCode == 550) {
            return false;
        }
        return true;
    }

    /**
     * Logs out and disconnects from the server
     *
     * @throws IOException thrown if any I/O error occurred.
     */
    public void logout() throws IOException {
        if (ftpClient != null && ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

    public Exception getException() {
        return exception;
    }

    public Object getResultObject() {
        return resultObject;
    }

}
