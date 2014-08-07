package xlong.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class wraps SHA-1 checksum algorithm for convenience.
 * <p>
 * SHA-1 produces a 160-bit (20-byte) hash value. A SHA-1 hash value 
 * is typically rendered as a hexadecimal number, 40 digits long. 
 * <p>
 * For more information, see 
 * <a href="http://en.wikipedia.org/wiki/SHA-1">Wiki SHA-1</a>.
 *
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public final class SHA1Util {
	
	/**
	 * Private constructor to make sure no instance of this class 
	 * will be created.
	 */
	private SHA1Util() {
		// will not be called
	}
	
	/** Maximum number of bytes to read in each loop.*/
	private static final int MAXBYTE = 1024;
	/** Constant for changing byte into numbers. */
	private static final int C1 = 0xff;
	/** Constant for changing byte into numbers. */
	private static final int C2 = 0x100;
	/** The base of the output integer. Always uses 16.*/
	private static final int BASE = 16;
	
	/**
	 * Gets the SHA-1 checksum of the file with given name.
	 * 
	 * @param filePath the path of the file to get checksum.
	 * @return the hexadecimal representation checksum of the file.
	 * @throws IOException if the file is not found or the cannot be read.
	 */
	public static String sha1Checksum(
			final Path filePath) 
			throws IOException {
		
        MessageDigest sha1 = null;
		try {
			sha1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
        FileInputStream fis = new FileInputStream(filePath.toString());
        byte[] data = new byte[MAXBYTE];
        int read = 0; 
        while ((read = fis.read(data)) != -1) {
            sha1.update(data, 0, read);
        }
        fis.close();
        
        byte[] hashBytes = sha1.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
          sb.append(Integer
        		  .toString((hashBytes[i] & C1) + C2, BASE)
        		  .substring(1));
        }
        
        return sb.toString();
	}
	
	/**
	 * Gets the SHA-1 checksum of the given string.
	 * 
	 * @param input the string to get checksum.
	 * @return the hexadecimal representation checksum of the string.
	 */
	public static String sha1Checksum(
			final String input) {
		
        MessageDigest sha1 = null;
		try {
			sha1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        
        byte[] hashBytes = sha1.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
          sb.append(Integer
        		  .toString((hashBytes[i] & C1) + C2, BASE)
        		  .substring(1));
        }
        
        return sb.toString();
	}

	/**
	 * Gets the SHA-1 checksum of the given bytesArray.
	 * 
	 * @param bytesArray the bytesArray to get checksum.
	 * @return the hexadecimal representation checksum of the string.
	 */
	public static String sha1Checksum(
			final byte[] bytesArray) {
		
        MessageDigest sha1 = null;
		try {
			sha1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        
        byte[] hashBytes = sha1.digest(bytesArray);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
          sb.append(Integer
        		  .toString((hashBytes[i] & C1) + C2, BASE)
        		  .substring(1));
        }
        
        return sb.toString();
	}
	
}
