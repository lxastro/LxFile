package xlong.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * This class wrap ZLIB compression for convenience.
 * <p>
 * This class can just deal with small files (smaller than around 1.3G).
 * If the outFile is exist, methods in this class will replace the exist one.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public final class CompressionUtil {

	/**
	 * Private constructor to make sure no instance of this class will be
	 * created.
	 */
	private CompressionUtil() {
		// will not be called
	}
	
	/** Maximum number of byte to read in each loop.*/
	private static final int MAXBYTE = 1024;
	
	/**
	 * Compress given byte array and output to the given path.
	 * The output directory must exist.
	 * If the outFile already exist, this method will do nothing.
	 * 
	 * @param byteArray the input string
	 * @param outFile the output file path
	 * @throws IOException if an I/O error occurs
	 */
	public static void compressByteArray(
			final byte[] byteArray, 
			final Path outFile) 
					throws IOException {
		if (Files.exists(outFile)) {
			return;
		}
		
		Deflater deflater = new Deflater();
		deflater.setInput(byteArray);

		BufferedOutputStream out = 
				new BufferedOutputStream(
						new FileOutputStream(outFile.toString()));
		
		deflater.finish();
		byte[] buffer = new byte[MAXBYTE];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			out.write(buffer, 0, count);
		}
		deflater.end();
		out.close();
	}

	
	/**
	 * Compress given input file and output to given output file.
	 * The inFile must exist.
	 * The output directory must exist.
	 * If the outFile already exist, this method will do nothing.
	 * 
	 * @param inFile the input file path
	 * @param outFile the output file path
	 * @throws IOException if the file is not found or the cannot be read.
	 */
	public static void compressFile(
			final Path inFile, 
			final Path outFile) 
					throws IOException {
		if (Files.exists(outFile)) {
			return;
		}
		
		byte[] data = toByteArray(inFile);
		compressByteArray(data, outFile);
	}
	
	/**
	 * Compress given string and output to the given path.
	 * The output directory must exist.
	 * If the outFile already exist, this method will do nothing.
	 * 
	 * @param string the input string
	 * @param outFile the output file name
	 * @throws IOException if an I/O error occurs
	 */
	public static void compressString(
			final String string, 
			final Path outFile) 
					throws IOException {
		if (Files.exists(outFile)) {
			return;
		}
		
		byte[] data = string.getBytes("ISO-8859-1");
		compressByteArray(data, outFile);
	}	

	/**
	 * Decompress given input file and output to given output file.
	 * The output directory must exist.
	 * If the inFile can not decompress, this method will fail.
	 * If the outFile already exist, this method will fail.
	 * 
	 * @param inFile the input file name
	 * @param outFile the output file name
	 * @throws IOException if an I/O error occurs
	 * @return success or not
	 */	
	public static boolean decompressToFile(
			final Path inFile, 
			final Path outFile) 
					throws IOException {
		if (Files.exists(outFile)) {
			return false;
		}
		
		byte[] data = toByteArray(inFile);
		Inflater inflater = new Inflater();
		inflater.setInput(data);

		BufferedOutputStream out = 
				new BufferedOutputStream(
						new FileOutputStream(outFile.toString()));
		
		byte[] buffer = new byte[MAXBYTE];
		while (!inflater.finished()) {
			int count;
			try {
				count = inflater.inflate(buffer);
			} catch (DataFormatException e) {
				e.printStackTrace();
				out.close();
				return false;
			}
			out.write(buffer, 0, count);
		}
		inflater.end();
		out.close();
		return true;
	}
	
	/**
	 * Decompress given input file and output to a byte array.
	 * If the inFile can not decompress, this method will return null.
	 * 
	 * @param inFile the input file path
	 * @return the output byte array
	 * @throws IOException if an I/O error occurs
	 */	
	public static byte[] decompressToByteArray(
			final Path inFile) 
					throws IOException {
		
		byte[] data = toByteArray(inFile);
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		
		StringBuilder outString = new StringBuilder();
		
		byte[] buffer = new byte[MAXBYTE];
		while (!inflater.finished()) {
			int count;
			try {
				count = inflater.inflate(buffer);
			} catch (DataFormatException e) {
				e.printStackTrace();
				return null;
			}
			outString.append(new String(buffer, 0, count, "ISO-8859-1"));
		}
		inflater.end();
		return outString.toString().getBytes("ISO-8859-1");
	}
	
	/**
	 * Decompress given input file and output to a string.
	 * If the inFile can not decompress, this method will return null.
	 * 
	 * @param inFile the input file path
	 * @return the output string
	 * @throws IOException if an I/O error occurs
	 */	
	public static String decompressToString(
			final Path inFile) 
					throws IOException {
		
		byte[] data = toByteArray(inFile);
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		
		StringBuilder outString = new StringBuilder();
		
		byte[] buffer = new byte[MAXBYTE];
		while (!inflater.finished()) {
			int count;
			try {
				count = inflater.inflate(buffer);
			} catch (DataFormatException e) {
				e.printStackTrace();
				return null;
			}
			outString.append(new String(buffer, 0, count, "ISO-8859-1"));
		}
		inflater.end();
		return outString.toString();
	}
	
	/**
	 * Reads a file and saves its content into a byte array.
	 * @param filePath the path of the file to read
	 * @return the byte array saves its content
	 * @throws IOException if an I/O error occurs
	 */
	private static byte[] toByteArray(final Path filePath) 
			throws IOException {  
	        byte[] buffer = null;  
            File file = new File(filePath.toString());  
            FileInputStream fis = new FileInputStream(file);  
            ByteArrayOutputStream bos = new ByteArrayOutputStream();  
            byte[] b = new byte[MAXBYTE];  
            int n;  
            while ((n = fis.read(b)) != -1) {  
                bos.write(b, 0, n);  
            }  
            fis.close();  
            bos.close();  
            buffer = bos.toByteArray();     
	        return buffer;  
	    }  
}
