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
 * JUnit test class for CompressionUtil class.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public class CompressionUtilTest {

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
				Files.deleteIfExists(dir.resolve("string.test"));
				Files.deleteIfExists(dir);
			} catch (IOException e) {
				e.printStackTrace();
				fail("Can't delete test files.");
			}
		} 
		System.out.println("Finish.");
	}
	
	/**
	 * Test method for 
	 * {@link xlong.util.CompressionUtil#compressFile(Path, Path)} 
	 * and
	 * {@link xlong.util.CompressionUtil#decompressToFile(Path, Path)}.
	 */
	@Test
	public final void testComDecFile() {
		String oriFile = "data/test/f.test";
		Path oriPath = Paths.get(oriFile);
		Path zipPath = Paths.get(oriFile + ".zlib");
		Path newPath = Paths.get(oriFile + "_new");
		String checksumOri = null;
		String checksumNew = null;
		try {
			CompressionUtil.compressFile(oriPath, zipPath);
			CompressionUtil.decompressToFile(zipPath, newPath);
			checksumOri = SHA1Util.sha1Checksum(oriPath);
			checksumNew = SHA1Util.sha1Checksum(newPath);
		} catch (IOException e) {
			org.junit.Assert.fail();
			e.printStackTrace();
		}
		assertEquals(checksumOri, checksumNew);
	}

	/**
	 * Test method for 
	 * {@link xlong.util.CompressionUtil#compressString(String, Path)} 
	 * and
	 * {@link xlong.util.CompressionUtil#decompressToString(Path)}.
	 */
	@Test
	public final void testComDecString() {
		String oriString = "This is a test string.";
		String oriFile = "data/test/string.test";
		String newString = null;
		Path newPath = Paths.get(oriFile);
		try {
			CompressionUtil.compressString(oriString, newPath);
			newString = CompressionUtil.decompressToString(newPath);
		} catch (IOException e) {
			e.printStackTrace();
			org.junit.Assert.fail();
		}
		assertEquals(oriString, newString);
	}
	
	
	/**
	 * Test method for 
	 * {@link xlong.util.CompressionUtil#compressByteArray(byte[], Path)} 
	 * and
	 * {@link xlong.util.CompressionUtil#decompressToByteArray(Path)}.
	 */
	@Test
	public final void testComDecByteArray() {
		String oriString = "This is a test string.";
		String oriFile = "data/test/string.test";
		byte[] oriBytes = oriString.getBytes();
		byte[] newBytes = null;
		Path newPath = Paths.get(oriFile);
		try {
			CompressionUtil.compressByteArray(oriBytes, newPath);
			newBytes = CompressionUtil.decompressToByteArray(newPath);
		} catch (IOException e) {
			e.printStackTrace();
			org.junit.Assert.fail();
		}
		
		assertEquals(oriBytes.length, newBytes.length);
		for (int i = 0; i < oriBytes.length; i++) {
			assertEquals(oriBytes[i], newBytes[i]);
		}
	}
}
