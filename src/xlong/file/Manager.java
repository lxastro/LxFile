package xlong.file;


/**
 * This is a father class of classes to manage objects and
 * operators. This class only contains properties don't need
 * serialization.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public abstract class Manager {
	/** the directory to store manager. */
	private static String managerDir = "data/manager";

	/**
	 * get manager directory.
	 * @return the managerDir
	 */
	public static String getManagerDir() {
		return managerDir;
	}

	/**
	 * set manager directory.
	 * @param dir the managerDir to set
	 */
	public static void setManagerDir(final String dir) {
		managerDir = dir;
	}

}
