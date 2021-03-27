package com.sr2.flopbox;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sr2.flopbox.common.CheckUtils;

public class CheckUtilsTest {
	private static final String TMP_DIR = "src/main/resources/tmp_dir";
	private static final String TMP_FILE_ONE = "src/main/resources/tmp_file";
	private static final String TMP_FILE_IMG = "src/main/resources/tmp_file.png";

	@BeforeClass
	public static void setUp() throws IOException {
		Files.createDirectory(Paths.get(TMP_DIR));
		Files.createFile(Paths.get(TMP_FILE_ONE));
		Files.createFile(Paths.get(TMP_FILE_IMG));
	}

	@AfterClass
	public static void tearDown() throws IOException {
		Files.deleteIfExists(Paths.get(TMP_DIR));
		Files.deleteIfExists(Paths.get(TMP_FILE_ONE));
		Files.deleteIfExists(Paths.get(TMP_FILE_IMG));
	}

	@Test
	public void checkIfStringIsNull_shouldReturnFalse_whenStringIsNull() {
		String nullString = null;

		boolean exist = CheckUtils.checkIfStringIsNull(nullString);

		assertFalse(exist);
	}

	@Test
	public void checkIfStringIsNull_shouldReturnFalse_whenStringIsEmpty() {
		String emptyString = "";

		boolean exist = CheckUtils.checkIfStringIsNull(emptyString);

		assertFalse(exist);
	}

	@Test
	public void checkIfStringIsNull_shouldReturnTrue_whenStringIsCorrect() {
		String string = "string";

		boolean exist = CheckUtils.checkIfStringIsNull(string);

		assertTrue(exist);
	}

	@Test
	public void checkIfStringIsInCorrectFormat_shouldReturnFalse_whenStringIsCorrect() {
		String correctString = "string";

		boolean exist = CheckUtils.checkIfStringIsInCorrectFormat(correctString);

		assertTrue(exist);
	}

	@Test
	public void checkIfStringIsInCorrectFormat_shouldReturnTrue_whenStringIsNotCorrect() {
		String wrongString = "string;test;ge;";

		boolean exist = CheckUtils.checkIfStringIsInCorrectFormat(wrongString);

		assertFalse(exist);
	}

	@Test
	public void checkIfLocalFileExist_shouldReturnTrue_whenFileExist() {
		boolean exist = CheckUtils.checkIfLocalFileExist(new File(TMP_FILE_ONE));

		assertTrue(exist);
	}

	@Test
	public void checkIfLocalFileExist_shouldReturnFalse_whenFileDoesNotExist() {
		boolean exist = CheckUtils.checkIfLocalFileExist(new File("wrongPath"));

		assertFalse(exist);
	}

	@Test
	public void checkIfLocalFileExist_shouldReturnFalse_whenRepositoryExist() {
		boolean exist = CheckUtils.checkIfLocalFileExist(new File(TMP_DIR));

		assertFalse(exist);
	}

	@Test
	public void checkIfLocalRepositoryExist_shouldReturnTrue_whenRepositoryExist() {
		boolean exist = CheckUtils.checkIfLocalRepositoryExist(TMP_DIR);

		assertTrue(exist);
	}

	@Test
	public void checkIfLocalRepositoryExist_shouldReturnFalse_whenFileExist() {
		boolean exist = CheckUtils.checkIfLocalRepositoryExist(TMP_FILE_ONE);

		assertFalse(exist);
	}

	@Test
	public void checkIfLocalRepositoryExist_shouldReturnFalse_whenRepositoryDoesNotExist() {
		boolean exist = CheckUtils.checkIfLocalRepositoryExist("wrongPath");

		assertFalse(exist);
	}
	
	@Test
	public void isImage_shouldReturnFalse_whenIsNotAImage() {
		boolean exist = CheckUtils.isImage(new File(TMP_FILE_ONE));

		assertFalse(exist);
	}
	
	@Test
	public void isImage_shouldReturnTrue_whenIsAImage() {
		boolean exist = CheckUtils.isImage(new File(TMP_FILE_IMG));

		assertTrue(exist);
	}
}
