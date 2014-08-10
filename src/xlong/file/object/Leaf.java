package xlong.file.object;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A leaf just contains a blob a blob checksum
 * and a boolean identify compressed or not.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public final class Leaf extends Container {

	/** for serialization. */
	private static final long serialVersionUID = -1719117201849838735L;
	
	/** the limit of compressing file. */
	private static final int COMPRESSLIM = 100000000;
	
	/** the checksum of the blob. */
	private String blobChecksum;
	
	/** the blob compressed or not. */
	private boolean compressed;
	
	/** the blob. */
	private transient Blob blob;
	
	/**
	 * Constructor just available in this class.
	 * Set the blob of the leaf.
	 * Set the type and checksum of the leaf. 
	 * @param inBlob the blob to contain.
	 * @param inCompressed the blob compressed or not.
	 */
	private Leaf(final Blob inBlob, final boolean inCompressed) {
		blob = inBlob;
		compressed = inCompressed;
		blobChecksum = blob.getChecksum();
		setType(Object.LEAF);
	}
	
	/**
	 * Create a leaf and save the leaf to file system.
	 * @param inBlob the blob to contain.
	 * @param inCompressed the blob compressed or not.
	 * @return the leaf
	 * @throws IOException if an I/O error occurs
	 */
	public static Leaf create(final Blob inBlob, final boolean inCompressed)
			throws IOException {
		Leaf leaf = new Leaf(inBlob, inCompressed);
		return leaf;
	}
	
	/**
	 * Create a leaf contains given file.
	 * 
	 * @param filePath the file to contain
	 * @return the tree. If fail, return null.
	 * @throws IOException if an I/O error occurs
	 */
	public static Leaf create(final Path filePath) throws IOException {
		if (Files.size(filePath) < COMPRESSLIM) {
			Blob blob = Blob.create(filePath, true);
			Leaf leaf = new Leaf(blob, true);
			return leaf;			
		} else {
			Blob blob = Blob.create(filePath, false);
			Leaf leaf = new Leaf(blob, false);
			return leaf;
		}
	}

	@Override
	public boolean restore(final Path outFilePath) throws IOException {
		System.out.println("Restore Leaf to " + outFilePath);
		return blob.restore(outFilePath, compressed);
	}

	@Override
	public String toString() {
		return "Leaf " + getChecksum() + "\n";
	}

	@Override
	public boolean recover() {
		blob = Blob.get(blobChecksum);
		return blob != null;
	}

	@Override
	public void backup() {
	}

	/**
	 * @return the blobChecksum
	 */
	public String getBlobChecksum() {
		return blobChecksum;
	}

}
