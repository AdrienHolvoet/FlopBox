package com.sr2.flopbox.common;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

/**
 * CheckUtils(Singleton pattern)Contains all the methods for checking parameters
 * through the FlopBox API
 * 
 * @author Adrien Holvoet
 */
public class CheckUtils {

	/**
	 * Method used to check if the String passed in parameter is not null
	 * 
	 * @param string
	 * @return False if null True otherwise
	 */
	public static boolean checkIfStringIsNull(String string) {
		return !(string == null || string.length() == 0);
	}

	/**
	 * Method used to check if the string passed in parameter does not contain
	 * Constant.SEPARATOR
	 * 
	 * @param string
	 * @return true if correct false otherwise
	 */
	public static boolean checkIfStringIsInCorrectFormat(String string) {
		Pattern pattern = Pattern.compile(Constant.SEPARATOR, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(string);
		return !matcher.find();
	}

	/**
	 * Method used to check if the file passed in parameter exist
	 * 
	 * @param filePath
	 * @return true if correct false otherwise
	 */
	public static boolean checkIfLocalFileExist(File file) {
		return file.exists() && file.isFile();
	}

	/**
	 * Method used to check if the repository passed in parameter exist
	 * 
	 * @param filePath
	 * @return true if not correct false otherwise
	 */
	public static boolean checkIfLocalRepositoryExist(String repo) {
		if (repo != null) {
			File file = new File(repo);
			return file.exists() && file.isDirectory();
		}
		return false;
	}

	/**
	 * Method used to check if the file passed in parameter is a image
	 * 
	 * @param file
	 * @return true if if image false otherwise
	 */
	public static boolean isImage(File file) {

		String mimeType = new MimetypesFileTypeMap().getContentType(file);
		String type = mimeType.split("/")[0].toLowerCase();
		return type.equals("image");

	}
}
