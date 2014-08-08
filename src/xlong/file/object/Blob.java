package xlong.file.object;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import xlong.util.CompressionUtil;
import xlong.util.SHA1Util;

/**
 * A blob object only contains the content of a file.
 * <p>
 * A blob object use the SHA-1 checksum of a file as its name.
 * To be specific, the name a blob is "checksum[0-1]/checksum[2-39]".
 * <p>
 * A blob object only contains the content of a file.
 * To be specific, blob stores the compressed content of a file
 * or stores the original content of a file.
 * Uses ZLIB compression in this class.
 * <p>
 * This blob class provide static methods for
 * creating a blob for a file or restoring a blot to a file.
 * <p>
 * Use create to get a blob.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public final class Blob extends Object {
	
	/**
	 * Constructor just available in this class.
	 * Set the type and checksum of the blob. 
	 * @param checksum the checksum of a file this blob stores.
	 */
	private Blob(final String checksum) {
		setChecksum(checksum);
		setType(Object.BLOB);
	}


	/**
	 * Save a file into a blob, 
	 * if the file not exist in the file system.
	 * If the file already exist, this method will calculate
	 * the checksum of exist file. If the checksum  of exist
	 * file equal to the checksum of new file and the
	 * compress is set to false, this method return original blob.
	 * And do similar when other situation.
	 * This method ensure the blob returned is wanted.
	 * Creates a new blob contains the checksum of the file.
	 * 
	 * @param filePath the file to save.
	 * @param compress compress the file or not.
	 * @return the created Blob
	 * @throws IOException if an I/O error occurs
	 */
	public static Blob create(
			final Path filePath, final boolean compress) 
					throws IOException {
		System.out.println("Create Blob for " + filePath);
		String checksum = SHA1Util.sha1Checksum(filePath);
		Blob blob = new Blob(checksum);
		
		Path outFilePath = blob.getPath();

		Files.createDirectories(outFilePath.getParent());
		
		if (compress) {
			// compress file
			if (Files.exists(outFilePath)) {
				String oriChecksum = SHA1Util.sha1Checksum(outFilePath);
				if (checksum != oriChecksum) {
					return blob;
				} else {
					Files.delete(outFilePath);
				}
			}
			System.out.println(
					"Compress " + filePath + " to " + outFilePath);
			CompressionUtil.compressFile(filePath, outFilePath); 
		} else {
			// copy file
			if (Files.exists(outFilePath)) {
				String oriChecksum = SHA1Util.sha1Checksum(outFilePath);
				if (checksum == oriChecksum) {
					return blob;
				} else {
					Files.delete(outFilePath);
				}
			}			
			System.out.println(
					"Copy " + filePath + " to " + outFilePath);
			Files.copy(filePath, outFilePath);
		}
		return blob;
	}
	
	/**
	 * Get a blob with given checksum.
	 * If the blob not exist in the file system, return null.
	 * @param checksum the checksum
	 * @return the blob
	 */
	public static Blob get(final String checksum) {
		Blob blob = new Blob(checksum);
		
		Path outFilePath = blob.getPath();
		if (Files.exists(outFilePath)) {
			return blob;
		} else {
			return null;
		}
	}
	

	/**
	 * Restore this blob to the file with given path.
	 * If a file is already exist,
	 * this method will rewrite the file.
	 * If the output directory not exist,
	 * this method will create the directory.
	 * 
	 * @param outFilePath the path of the file restores to
	 * @param compressed the blob is compressed or not
	 * @return success or not
	 * @throws IOException if an I/O error occurs
	 */
	public boolean restore(
			final Path outFilePath, final boolean compressed) 
			throws IOException {
		Path inFilePath = getPath();
		System.out.println("Restore Blob to " + outFilePath);
		
		Files.createDirectories(outFilePath.getParent());

		if (compressed) {
			System.out.println(
					"Decompress " + inFilePath + " to " + outFilePath);
			if (!CompressionUtil.decompressToFile(inFilePath, outFilePath)) {
				return false;
			}
		} else {
			System.out.println(
					"Copy " + inFilePath + " to " + outFilePath);
			Files.copy(inFilePath, outFilePath,
					StandardCopyOption.REPLACE_EXISTING);
		}
		return true;
	}
	
	/**
	 * Converts blob to string.
	 * @return the string
	 */
	@Override
	public String toString() {
		return "Blob " + getChecksum() + "\n";	
	}
}
