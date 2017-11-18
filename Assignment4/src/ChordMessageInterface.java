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
	 * @param stream
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
	
	public void emitMap(long key, String value, Counter counter)throws RemoteException;
	
	public void emitReduce(long key, String value, Counter counter) throws RemoteException;
	
	public void mapContext(long page, MapInterface mapper, Counter counter) throws RemoteException;
	
	public void reduceContext(long source, ReduceInterface reducer, Counter counter) throws RemoteException;
	
	public void completed(long source, Counter counter) throws RemoteException;
}
