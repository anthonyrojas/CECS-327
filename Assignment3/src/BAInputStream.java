import java.io.*;
import java.nio.*;

/**
 * This class acts as a serializable ByteArrayInputStream
 */
public class BAInputStream extends InputStream implements Serializable {
	/**
	 * The byte array containing data of an object
	 */
	private byte[] data;
	/**
	 * The length of the byte array
	 */
	private int count;
	/**
	 * The current marked position within the stream
	 */
	private int mark;
	/**
	 * The index of the next character to be read from the data
	 */
	private int pos;

	/**
	 * Creates a new serializable ByteArrayInputStream
	 * 
	 * @param d
	 *            The data
	 */
	public BAInputStream(byte[] d) {
		data = d;
		count = data.length;
		pos = 0;
	}

	/**
	 * Redas the next byte of data from the input stream
	 * 
	 * @return The next byte in the data
	 */
	public synchronized int read() {
		if (pos < count) {
			return data[pos++] & 0xff;
		}
		return -1;
	}

	/**
	 * Returns the number of remaining bytes that can be read from the data byte
	 * array
	 * 
	 * @return Bytes not read
	 */
	public synchronized int available() {
		return count - pos;
	}
}
