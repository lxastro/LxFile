package xlong.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test class for SHA1Util class.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public class SHA1Test {

	/** the maximum number write to test file. */
	private static final int MAXNUM = 1000000;

	/**
	 * Create test directory and files before test.
	 * Create a test directory /data/test/
	 * If the directory already exist, the test will fail.
	 * Create a test file f.test in test directory.
	 */
	@BeforeClass
	public static final void before() {
		System.out.println("Preparing test file...");
		Path dir = Paths.get("data/test");
		if (Files.isDirectory(dir)) {
			fail("The test directory already exist."
				+ "Please delete the directory \"data/test\" and retry.");
		} else {
			try {
				Files.createDirectories(dir);
			} catch (IOException e) {
				fail("Can't create the test directory.");
				e.printStackTrace();
			}
		}
		try {
			BufferedWriter out =
					new BufferedWriter(
							new FileWriter("data/test/f.test"));
			for (int i = 1; i < MAXNUM; i++) {
				out.write(Integer.toString(i) + "\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("Can't create the test file.");
		}
	}
	
	/**
	 * Delete test directory and all files in it.
	 */
	@AfterClass
	public static final void after() {
		System.out.println("Deleting test files...");
		Path dir = Paths.get("data/test");
		if (Files.isDirectory(dir)) {
			try {
				Files.deleteIfExists(dir.resolve("f.test"));
				Files.deleteIfExists(dir.resolve("f.test_new"));
				Files.deleteIfExists(dir.resolve("f.test.zlib"));
				Files.deleteIfExists(dir);
			} catch (IOException e) {
				e.printStackTrace();
				fail("Can't delete test files.");
			}
		} 
		System.out.println("Finish.");
	}
	
	/**
	 * Test method for {@link xlong.util.SHA1Util#sha1Checksum(String)}.
	 */
	@Test
	public final void testSha1Checksum() {
		Path filePath = Paths.get("data/test/f.test");
		String sha = null;
		try {
			sha = SHA1Util.sha1Checksum(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String resSHA = "fcad7796ad39bbd1a6b4b77a8d0a5a0bfcd2ba5e";
		assertEquals(sha, resSHA);
	}

	/**
	 * Test method for {@link xlong.util.SHA1Util#sha1Checksum(String)}.
	 */
	@Test
	public final void testSha1ChecksumString() {
		String input = "doc/resources/background.gif";
		String shaT = null;
		shaT = SHA1Util.sha1Checksum(input);
		String resSHAT = "07262c761b8486faee07a26b1a440f1bccc21104";
		assertEquals(shaT, resSHAT);
	}

	/**
	 * Test method for {@link xlong.util.SHA1Util#sha1Checksum(byte[])}.
	 */
	@Test
	public final void testSha1ChecksumByteArray() {
		String input = "doc/resources/background.gif";
		String shaT = null;
		shaT = SHA1Util.sha1Checksum(input.getBytes());
		String resSHAT = "07262c761b8486faee07a26b1a440f1bccc21104";
		assertEquals(shaT, resSHAT);
	}
}
