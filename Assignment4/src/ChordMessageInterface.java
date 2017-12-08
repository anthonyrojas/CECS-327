import java.rmi.*;
import java.io.*;

/**
 * This class is an interface for a Chord
 */
public interface ChordMessageInterface extends Remote {

	/**
	 * Accessor for preceding node
	 * 
	 * @return The predecessor node to the current node
	 */
	public ChordMessageInterface getPredecessor() throws RemoteException;

	/**
	 * Locates the successor node to the current node
	 * 
	 * @param key
	 *            The guid of the current node
	 * @return The successor node
	 */
	ChordMessageInterface locateSuccessor(long key) throws RemoteException;

	/**
	 * Retrieves the successor node to the current node in the ring
	 * 
	 * @param key
	 *            The guid of the current node
	 * @return The successor node
	 */
	ChordMessageInterface closestPrecedingNode(long key) throws RemoteException;

	/**
	 * Joins the node onto a different ring on the specified port and ip
	 * 
	 * @param Ip
	 *            The ip address of the chord
	 * @param port
	 *            The port number
	 */
	public void joinRing(String Ip, int port) throws RemoteException;

	/**
	 * Sends a message to notify a peer node of changes
	 * 
	 * @param j
	 *            The node that will be notified
	 */
	public void notify(ChordMessageInterface j) throws RemoteException;

	/**
	 * Checks if a node is still active within the chord ring
	 * 
	 * @return Boolean value indicating if the node is active
	 */
	public boolean isAlive() throws RemoteException;

	/**
	 * Method for obtaining the id of the current chord node
	 * 
	 * @return The guid of the chord node
	 */
	public long getId() throws RemoteException;

	/**
	 * API command for storing data in the current chord registry
	 * 
	 * @param guidObject
	 *            The metadata file name of the current chord registry
	 * @param inputStream
	 *            The data being entered into the metadata
	 */
	public void put(long guidObject, InputStream inputStream) throws IOException, RemoteException;

	/**
	 * API command for retreiving data from the chord registry's metadata
	 * 
	 * @param guidObject
	 *            The file name for the metadata
	 * @return A filestream with the metadata
	 */
	public InputStream get(long guidObject) throws IOException, RemoteException;

	/**
	 * API for deleting data from the current chord registry
	 * 
	 * @param guidObject
	 *            The file name of the metadata
	 */
	public void delete(long guidObject) throws IOException, RemoteException;

	/**
	 * Obtains the bytes from a specified file in the registry
	 * @param guidObject The name of the file to be converted to bytes
	 * @return The bytes of a file
	 * */
	public byte[] getBytes(long guidObject) throws RemoteException, IOException;

	/**
	 * Emits the key and value to the map srtucture.
	 * @param key The guid of the word
	 * @param counter The counter interface to be used with the mapping phase
	 * @param value The word being counted
	 * */
	public void emitMap(long key, String value, CounterInterface counter)throws RemoteException;

	/**
	 * Emits the key and value to the reduce structure
	 * @param key The md5 value of the word
	 * @param value The word being added to the reduce structure
	 * @param counter The counter associated with the reduce phase
	 * */
	public void emitReduce(long key, String value, CounterInterface counter) throws RemoteException;

	/**
	 * Maps the context of the raw page file to the map structure
	 * @param page The guid of the page
	 * @param mapper The mapper interface being used to fill the map structure
	 * @param counter The counter associated with the map phase
	 * */
	public void mapContext(long page, MapInterface mapper, CounterInterface counter) throws RemoteException, IOException;

	/**
	 * Reduces the context of the map structure by counting the words into the reduce structure
	 * @param source The guid of the of the chord
	 * @param reducer The reducer for filling in the reduce structure
	 * @param counter The counter associated with the reduce phase
	 * */
	public void reduceContext(long source, ReduceInterface reducer, CounterInterface counter) throws RemoteException, IOException;

	/**
	 * Outputs the reduce data to a file after the reduce phase is complete
	 * @param source The guid of the chord
	 * @param counter The counter associated with the complete phase
	 * @param outputGuid The name of the file being map reduced from
	 * */
	public void completed(long source, CounterInterface counter, long outputGuid) throws RemoteException;
}
