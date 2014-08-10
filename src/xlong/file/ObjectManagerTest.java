package xlong.file;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xlong.file.object.Object;

/**
 * test ObjectManager.
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public class ObjectManagerTest {

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
		Manager.setManagerDir("data/test/manager");
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
	 * test.
	 */
	@Test
	public final void test() {
		ObjectManager.loadManager();
		if (!ObjectManager.operate(new String[]{"Import", "src", "Codes"})) {
			fail();
		}
		ObjectManager.saveManager();
		ObjectManager.loadManager();
		System.out.print(ObjectManager.listRoot());
		System.out.print(ObjectManager.listFather());
	}

}
