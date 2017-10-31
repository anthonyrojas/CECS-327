import java.io.*;
import java.nio.*;

/**
 * This class reads data into the metadata file
 */
public class FileStream extends InputStream implements Serializable {
	/**
	 * The current position from which to read the byteBuffer
	 */
	private int currentPosition;
	/**
	 * The data stored as a byte array
	 */
	private byte[] byteBuffer;
	/**
	 * The size of the byteBuffer
	 */
	private int size;

	/**
	 * The constructor for a file stream that initializes an metadata file if it
	 * does not exist
	 * 
	 * @param pathName
	 *            is the path and name of the metadata file
	 */
	public FileStream(String pathName) throws FileNotFoundException, IOException {
		File file = new File(pathName);
		if (!file.exists()) {
			file.createNewFile();
		}
		size = (int) file.length();
		byteBuffer = new byte[size];
		FileInputStream fileInputStream = new FileInputStream(pathName);
		int i = 0;
		while (fileInputStream.available() > 0) {
			byteBuffer[i++] = (byte) fileInputStream.read();
		}
		fileInputStream.close();
		currentPosition = 0;
	}

	/**
	 * Default constructor resets the position back to 0
	 */
	public FileStream() throws FileNotFoundException {
		currentPosition = 0;
	}

	/**
	 * Reads the current byte in the buffer
	 * 
	 * @return The byte as in int at the current position
	 */
	public int read() throws IOException {
		if (currentPosition < size)
			return (int) byteBuffer[currentPosition++];
		return 0;
	}

	/**
	 * Checks if there are more bytes to be read from the buffer
	 * 
	 * @return The number of remaining bytes in the buffer
	 */
	public int available() throws IOException {
		return size - currentPosition;
	}
}