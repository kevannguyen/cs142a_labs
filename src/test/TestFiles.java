package test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Scanner;

import org.junit.*;
import static org.junit.Assert.*;

import crux.*;

public class TestFiles {

	/* Path to test files root dir. */
	private static final String fileRoot = "tests";

	/* Compiler to use. */
	private crux.Compiler compiler;

	/* Original output stream. */
	private PrintStream outOrg;

	/* Original err stream. */
	private PrintStream errOrg;

	/* Buffer stdout stream. */
	private ByteArrayOutputStream outBuffer;

	/* Buffer stderr stream. */
	private ByteArrayOutputStream errBuffer;

	/* Our output stream. */
	private PrintStream outStream;

	/* Our err stream. */
	private PrintStream errStream;

	@Before
	public void setUp() {
		compiler = new crux.Compiler();	
		outOrg = System.out;
		errOrg = System.err;
		outBuffer = new ByteArrayOutputStream();
		errBuffer = new ByteArrayOutputStream();
		outStream = new PrintStream(outBuffer);
		errStream = new PrintStream(errBuffer);
	}

	@After
	public void tearDown() {
		compiler = null;
		useOutBuffer(false);
	}

	@Test
	public void testLab1Public() {
		testFilesIn("lab1/public");
	}

	@Test
	public void testLab1Private() {
		testFilesIn("lab1/private");
	}

	@Test
	public void testLab1PrivateSecret() {
		testFilesIn("lab1/private_secret");
	}

	private void testFilesIn(String subdir) {
		File dir = new File(fileRoot + "/" + subdir);
		String[] cruxFiles = dir.list(new FilenameFilter() {
 	 	 	public boolean accept(File dir, String name) {
 	 	 		return name.matches(".*\\.crx$");
 	 	 	}
		});

		for (String cruxFile: cruxFiles) {
			String[] nameParts = cruxFile.split("\\.");
			if (nameParts.length != 2) {
				System.err.println("File format changed?");
				System.exit(1);
			}
			String outFile = nameParts[0] + ".out";
			testFile(fileRoot + "/" + subdir + "/" + cruxFile, fileRoot + "/" + subdir + "/" + outFile);
		}
		System.out.printf("In %s: ", subdir);
		System.out.printf("All tests files passed!\n");
	}

	/**
	 * Compile input file and compare with reference.
	 * @param cruxFileName The input crux file.
	 * @param outFileName The reference expected output.
	 */
	private void testFile(String cruxFileName, String outFileName) {
		System.out.printf("Testing input file \"%s\", with the expected output in \"%s\"\n", cruxFileName, outFileName);
		useOutBuffer(true);
		compiler.compile(cruxFileName);
		String actual = outBuffer.toString();
		String errStr = errBuffer.toString();
		useOutBuffer(false);
		if (!errStr.isEmpty()) {
			System.err.printf("Compiler gave this error output: {\\n%s\\n}\\n", errStr);
		}

		File outFile = new File(outFileName);
		java.util.Scanner outScanner = null;
		try {
			outScanner = new java.util.Scanner(outFile);
		} catch (FileNotFoundException fnfe) {
			System.err.printf("Bad filename \"%s\"!\n", outFileName);
			return;
		}
		String expected = outScanner.useDelimiter("\\Z").next();
		expected += '\n'; // Needed apparently.

		actual = actual.replaceAll("\\r", ""); // So tests can be run under Windoze.
		if (!expected.equals(actual)) {
			System.out.println("Wrong compiler output.");
			System.out.printf("exp={\n%s\n}\nact={\n%s\n}\n", expected, actual);
			fail();
		}
		//assertEquals("Wrong compiler output.", expected+'\n', actual);
	}

	/**
	 * Toggles the output buffers.
	 * @param active If the buffers should be used or not.
	 */
	private void useOutBuffer(boolean active) {
		if (active) {
			System.setOut(outStream);
			System.setErr(errStream);
		} else {
			try {
				outBuffer.flush();
				errBuffer.flush();
			} catch (IOException ioe) {
				System.err.println("Could not flush buffers.");
				System.exit(1);
			}
			outBuffer.reset();
			errBuffer.reset();
			System.setOut(outOrg);
			System.setErr(errOrg);
		}
	}
}
