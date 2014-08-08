package xlong.file.object;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import xlong.util.CompressionUtil;
import xlong.util.SHA1Util;

/**
 * The abstract class of all kinds of container.
 * Container just contain reference of files.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public abstract class Container extends Object implements  Serializable {

	/** for serialization. */
	private static final long serialVersionUID = -8577017595565223283L;
	
	/**
	 * Use serialization change the object into byte array.
	 * 
	 * @return the byte array
	 */
	protected final byte[] toByteArray() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(bs);
			os.writeObject(this);
		} catch (IOException e) {
			e.printStackTrace();
		}   
        byte[] bytesArray = bs.toByteArray();
        return bytesArray;
	}
	
	/**
	 * Save this object to file system. Use serialization change the object
	 * into byte array.Then calculate, set and return the SHA1-checksum.
	 * Finally save the compress the string to a file.
	 * If the directory not exist, this method will create the directory.
	 * 
	 * @return SHA-1 checksum of this object
	 * @throws IOException if an I/O error occurs
	 */
	public final String save() throws IOException {
		backup();
		calChecksum();
		System.out.println("Save container " + getChecksum());
		
        byte[] bytesArray = toByteArray();
		Path outFilePath = getPath();
		Files.createDirectories(outFilePath.getParent());
        CompressionUtil.compressByteArray(bytesArray, outFilePath);
        
        return getChecksum();
	}
	
	/**
	 * Load the backup object.
	 * If fail return null.
	 * 
	 * @param checksum the checksum
	 * @return the backup object
	 * @throws IOException if an I/O error occurs
	 */
	public static final Container load(final String checksum)
			throws IOException {
		System.out.println("Load container " + checksum);
		
		Path filePath = Object.checksumToPath(checksum);
		byte[] bytesArray = CompressionUtil.decompressToByteArray(filePath);	
		
		ByteArrayInputStream bi = new ByteArrayInputStream(bytesArray);
        ObjectInputStream oi = new ObjectInputStream(bi);   

        Container con = null;
        try {
			con = (Container) oi.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
        if (con.recover()) {
        	return con;
        } else {
        	return null;
        }
	}
	
	/**
	 * Calculate the checksum of this tree.
	 * Notice Blob object must not use this method.
	 * The checksum of Blob can not modify.
	 */
	protected final void calChecksum() {
		byte[] byteArray = toByteArray();
		String newChecksum = SHA1Util.sha1Checksum(byteArray);
		setChecksum(newChecksum);		
	}
	
	/**
	 * Restore the container to the given directory.
	 * If a file is already exist,
	 * this method will rewrite the file.
	 * If the output directory not exist,
	 * this method will create the directory.
	 * 
	 * @param outFilePath the path of the file restores to
	 * @return totally success or not.
	 * @throws IOException if an I/O error occurs
	 */
	public abstract boolean restore(Path outFilePath) throws IOException;
	
	/**
	 * Backup transient properties when save.
	 * @throws IOException if an I/O error occurs
	 */
	public abstract void backup() throws IOException;
	
	/**
	 * Recover transient properties when load.
	 * @return success or not
	 */
	public abstract boolean recover();
	
	@Override
	public abstract String toString();

}
