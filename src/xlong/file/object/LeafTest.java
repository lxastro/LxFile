package xlong.file.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xlong.util.SHA1Util;

/**
 * JUnit test class for Leaf class.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public class LeafTest {

	/** the maximum number write to test file. */
	private static final int MAXNUM = 1000000;

	/**
	 * Create test directory and files before test.
	 * Create a test directory data/test/
	 * If the directory already exist, the test will fail.
	 * Create a test file f.test in test directory.
	 * Set objectDir of Object Class to data/test/object
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
		Object.setObjectDir("data/test/object");
	}
	
	/**
	 * Delete test directory and all files in it.
	 */
	@AfterClass
	public static final void after() {
		System.out.println("Deleting test files...");
		Path dir = Paths.get("data/test");
		if (Files.isDirectory(dir)) {
			if (!deleteFile(dir.toFile())) {
				fail("Can't delete test files.");
			}
		} 
		System.out.println("Finish.");
	}
	
	/**
	 * Delete a file or directory.
	 * @param file the file to delete
	 * @return success or not
	 */
	private static boolean deleteFile(final File file) {
		if (file.exists()) {
			if (file.isFile()) {
				return file.delete();
			} else if (file.isDirectory()) {
				boolean flag = true;
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					flag &= deleteFile(files[i]);
				}
				flag &= file.delete();
				return flag;
			}
		}
		return false;
	}
	
	/**
	 * Test.
	 */
	@Test
	public final void testLeaf() {
		String oriFile = "data/test/f.test";
		Path oriPath = Paths.get(oriFile);		
		Path newPath = Paths.get(oriFile + "_new");
		String checksumOri = null;
		String checksumNew = null;
		try {
			Blob blob = Blob.create(oriPath, true);
			Container leaf = Leaf.create(blob, true);
			String leafChecksum = leaf.save();
			Container newLeaf = Leaf.load(leafChecksum);
			newLeaf.restore(newPath);
			checksumOri = SHA1Util.sha1Checksum(oriPath);
			checksumNew = SHA1Util.sha1Checksum(newPath);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		assertEquals(checksumOri, checksumNew);
	}
	
	/**
	 * Test.
	 */
	@Test
	public final void testLeafDirect() {
		String oriFile = "data/test/f.test";
		Path oriPath = Paths.get(oriFile);		
		Path newPath = Paths.get(oriFile + "_new");
		String checksumOri = null;
		String checksumNew = null;
		try {
			Container leaf = Leaf.create(oriPath);
			
			String leafChecksum = leaf.save();
			Container newLeaf = Leaf.load(leafChecksum);
			newLeaf.restore(newPath);
			checksumOri = SHA1Util.sha1Checksum(oriPath);
			checksumNew = SHA1Util.sha1Checksum(newPath);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		assertEquals(checksumOri, checksumNew);
	}
}
