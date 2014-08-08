package xlong.file.object;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The abstract class of all kinds of file object classes.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public abstract class Object implements Comparable<Object> {
	
	/** type constant. */
	public static final int BLOB = 1;
	/** type constant. */
	public static final int LEAF = 2;
	/** type constant. */
	public static final int TREE = 3;
	
	/** the directory to store objects. */
	private static String objectDir = "data/object";
	
	/**
	 * Sets the object directory.
	 * @param dir the object directory wants to set.
	 */
	public static final void setObjectDir(final String dir) {
		objectDir = dir;
	}
	
	/**
	 * Gets the object directory. 
	 * @return the object directory.
	 */
	public static final String getObjectDir() {
		return objectDir;
	}
	
	/** the type of this object. BLOB, LEAF or TREE. */
	private int type = 0;
	
	/** the SHA-1 checksum of this object. */
	private String checksum = null;

	/**
	 * Set type.
	 * @param newType the type to set
	 */
	protected final void setType(final int newType) {
		type = newType;
	}
	
	/**
	 * Sets the checksum of this object.
	 * @param sha1Checksum the SHA-1 checksum to set
	 */
	protected final void setChecksum(final String sha1Checksum) {
		checksum = sha1Checksum;
	}
	

	
	/**
	 * Get type.
	 * @return the type
	 */
	public final int getType() {
		return type;
	}
	
	/**
	 * Gets the checksum of this object.
	 * @return the checksum of this object.
	 */
	public final String getChecksum() {
		return checksum;
	}

	/**
	 * Gets the path of this object.
	 * @return the path of this object.
	 */
	public final Path getPath() {
		return checksumToPath(checksum);
	}
	

	
	@Override
	public abstract String toString();
	
	/**
	 * Compares one object to another.
	 * First compare their type.
	 * Then compare their checksum.
	 * 
	 * @param o the object compare to
	 * @return the compare result.
	 */
	@Override
	public final int compareTo(final Object o) {
		if (type == o.type) {
			return checksum.compareTo(o.checksum);
		} else {
			return type - o.type;
		}
	}
	
	/**
	 * Converts checksum to path.
	 * @param checksum checksum
	 * @return the path
	 */
	public static final Path checksumToPath(final String checksum) {
		return Paths.get(objectDir,
				checksum.substring(0, 2), checksum.substring(2));
	}
}
