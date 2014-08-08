package xlong.file.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
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
public class TreeTest {

	/**
	 * Create test directory and files before test.
	 * Create a test directory data/test/
	 * If the directory already exist, the test will fail.
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
	public final void testTree() {
		Path oriFile = Paths.get("src/xlong/file/object/TreeTest.java");
		Path newFile = Paths.get("data/test/xlong/file/object/TreeTest.java");
		Path oriPath = Paths.get("src");		
		Path newPath = Paths.get("data/test/");
		String checksumOri = null;
		String checksumNew = null;
		try {
			Tree tree = Tree.create(oriPath);
			String treeChecksum = tree.save();
			System.out.println(tree.listAll());
			Tree newTree = (Tree) Tree.load(treeChecksum);
			newTree.restore(newPath);
			checksumOri = SHA1Util.sha1Checksum(oriFile);
			checksumNew = SHA1Util.sha1Checksum(newFile);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(checksumOri, checksumNew);
	}
	
	/**
	 * Test.
	 */
	@Test
	public final void testTreeDelete() {
		Path nick1 = Paths.get("xlong/file/object/TreeTest.java");
		Path nick2 = Paths.get("xlong/util");
		Path file0 = Paths.get("data/test/n/xlong/file/object/Tree.java");
		Path file1 = Paths.get("data/test/n/xlong/file/object/TreeTest.java");
		Path file2 = Paths.get("data/test/n/xlong/util/package-info.java");
		Path oriPath = Paths.get("src");		
		Path newPath = Paths.get("data/test/n");
		try {
			Tree tree = Tree.create(oriPath);
			tree.save();
			tree.deleteLeaf(nick1);
			tree.deleteTree(nick2);
			String treeChecksum = tree.save();
			System.out.println(tree.listAll());
			Tree newTree = (Tree) Tree.load(treeChecksum);
			newTree.restore(newPath);
			if (!Files.exists(file0)) {
				fail();
			}
			if (Files.exists(file1) || Files.exists(file2)) {
				fail();
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
