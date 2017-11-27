import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * This class creates a chord messenger node and implements the
 * ChordMessageInterface class
 */
public class Chord extends java.rmi.server.UnicastRemoteObject implements ChordMessageInterface, Serializable {
	/**
	 * Integer value for Remote interface
	 */
	public static final int M = 2;

	/**
	 * RMI registry lookup for remote objects
	 */
	Registry registry; // rmi registry for lookup the remote objects.
	/**
	 * The successor node to the current chord
	 */
	ChordMessageInterface successor;

	/**
	 * The predecessor node the current chord object
	 */
	ChordMessageInterface predecessor;

	/**
	 * The fingers for the finger table
	 */
	ChordMessageInterface[] finger;

	/**
	 * The next finger to be added into the finger array
	 */
	int nextFinger;

	/**
	 * The finger table for the ring
	 */
	ChordMessageInterface fingerTable[];
	/**
	 * Unique identifier for the current chord object
	 */
	long guid; // GUID (i)
	Counter counter;
	TreeMap<Long, List<String>> mapStruct;
	TreeMap<Long, String> reduceStruct;
	/**
	 * Checks if 2 nodes are close or neighbors to each other in a semi-closed
	 * interval
	 * 
	 * @param key
	 *            distance between 2 nodes
	 * @param key1
	 *            the first node
	 * @param key2
	 *            the second node
	 * @return True or false that 2 nodes are close or neighbors
	 */
	public Boolean isKeyInSemiCloseInterval(long key, long key1, long key2) {
		if (key1 < key2)
			return (key > key1 && key <= key2);
		else
			return (key > key1 || key <= key2);
	}

	/**
	 * Checks if 2 nodes are close to each other or neighbors in an open
	 * interval
	 * 
	 * @param key
	 *            The distance between the 2 nodes
	 * @param key1
	 *            The first node
	 * @param key2
	 *            The second node
	 * @return Boolean value as to whether two node are close to each other
	 */
	public Boolean isKeyInOpenInterval(long key, long key1, long key2) {
		if (key1 < key2)
			return (key > key1 && key < key2);
		else
			return (key > key1 || key < key2);
	}

	/**
	 * API command for storing data in the current chord registry
	 * 
	 * @param guidObject
	 *            The metadata file name of the current chord registry
	 * @param stream
	 *            The data being entered into the metadata
	 */
	public void put(long guidObject, InputStream stream) throws RemoteException {
		try {
			String fileName = "./" + guid + "/repository/" + guidObject;
			FileOutputStream output = new FileOutputStream(fileName);
			while (stream.available() > 0)
				output.write(stream.read());
			output.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * API command for retreiving data from the chord registry's metadata
	 * 
	 * @param guidObject
	 *            The file name for the metadata
	 * @return A filestream with the metadata
	 */
	public InputStream get(long guidObject) throws RemoteException {
		FileStream file = null;
		try {
			file = new FileStream("./" + guid + "/repository/" + guidObject);
		} catch (IOException e) {
			throw (new RemoteException("File does not exists"));
		}
		return file;
	}

	/**
	 * API for deleting data from the current chord registry
	 * 
	 * @param guidObject
	 *            The file name of the metadata
	 */
	public void delete(long guidObject) throws RemoteException {
		File file = new File("./" + guid + "/repository/" + guidObject);
		file.delete();
	}

	/**
	 * Method for obtaining the id of the current chord node
	 * 
	 * @return The guid of the chord node
	 */
	public long getId() throws RemoteException {
		return guid;
	}

	/**
	 * Checks if a node is still active within the chord ring
	 * 
	 * @return Boolean value indicating if the node is active
	 */
	public boolean isAlive() throws RemoteException {
		return true;
	}

	/**
	 * Get preceding node within the ring
	 * 
	 * @return The prededing node to the current node
	 */
	public ChordMessageInterface getPredecessor() throws RemoteException {
		return predecessor;
	}

	/**
	 * Finds the successor to the current node within the ring
	 * 
	 * @param key
	 *            The guid of the node that will be the predecessor or the
	 *            current node
	 * @return The guid of the successor node
	 */
	public ChordMessageInterface locateSuccessor(long key) throws RemoteException {
		if (key == guid)
			throw new IllegalArgumentException("Key must be distinct that  " + guid);
		if (successor.getId() != guid) {
			if (isKeyInSemiCloseInterval(key, guid, successor.getId()))
				return successor;
			ChordMessageInterface j = closestPrecedingNode(key);

			if (j == null)
				return null;
			return j.locateSuccessor(key);
		}
		return successor;
	}

	/**
	 * Finds the closest preceding node to the current node within the ring
	 * 
	 * @param key
	 *            The current node's guid
	 * @return The closest preceding node
	 */
	public ChordMessageInterface closestPrecedingNode(long key) throws RemoteException {
		// todo
		if (key != guid) {
			int i = M - 1;
			while (i >= 0) {
				try {

					if (isKeyInSemiCloseInterval(finger[i].getId(), guid, key)) {
						if (finger[i].getId() != key)
							return finger[i];
						else {
							return successor;
						}
					}
				} catch (Exception e) {
					// Skip ;
				}
				i--;
			}
		}
		return successor;
	}

	/**
	 * Adds a new node to the chord ring and sets the new successor and
	 * predecessor
	 * 
	 * @param ip
	 *            The node's ip address
	 * @param port
	 *            The port on which the chord exists
	 */
	public void joinRing(String ip, int port) throws RemoteException {
		try {
			System.out.println("Get Registry to joining ring");
			Registry registry = LocateRegistry.getRegistry(ip, port);
			ChordMessageInterface chord = (ChordMessageInterface) (registry.lookup("Chord"));
			predecessor = null;
			// predecessor = chord.getPredecessor();
			successor = chord.locateSuccessor(this.getId());
			System.out.println("Joining ring");
		} catch (RemoteException | NotBoundException e) {
			successor = this;
		}
	}

	/**
	 * Locates the next successor to the current node
	 */
	public void findingNextSuccessor() {
		int i;
		successor = this;
		for (i = 0; i < M; i++) {
			try {
				if (finger[i].isAlive()) {
					successor = finger[i];
				}
			} catch (RemoteException | NullPointerException e) {
				finger[i] = null;
			}
		}
	}

	/**
	 * Balances the ring in the event of a new node or a node leaving
	 */
	public void stabilize() {
		try {
			if (successor != null) {
				ChordMessageInterface x = successor.getPredecessor();

				if (x != null && x.getId() != this.getId()
						&& isKeyInOpenInterval(x.getId(), this.getId(), successor.getId())) {
					successor = x;
				}
				if (successor.getId() != getId()) {
					successor.notify(this);
				}
			}
		} catch (RemoteException | NullPointerException e1) {
			findingNextSuccessor();

		}
	}

	/**
	 * Sends a message to notify a peer node of changes
	 * 
	 * @param j
	 *            The node that will be notified
	 */
	public void notify(ChordMessageInterface j) throws RemoteException {
		if (predecessor == null || (predecessor != null && isKeyInOpenInterval(j.getId(), predecessor.getId(), guid)))
			predecessor = j;
		try {
			File folder = new File("./" + guid + "/repository/");
			File[] files = folder.listFiles();
			for (File file : files) {
				long guidObject = Long.valueOf(file.getName());
				if (guidObject < predecessor.getId() && predecessor.getId() < guid) {
					predecessor.put(guidObject, new FileStream(file.getPath()));
					file.delete();
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// happens sometimes when a new file is added during foreach loop
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Updates the position of the fingers in the chord
	 */
	public void fixFingers() {

		long id = guid;
		try {
			long nextId = this.getId() + 1 << (nextFinger + 1);
			finger[nextFinger] = locateSuccessor(nextId);

			if (finger[nextFinger].getId() == guid)
				finger[nextFinger] = null;
			else
				nextFinger = (nextFinger + 1) % M;
		} catch (RemoteException | NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the preceding node is active and/or alive
	 */
	public void checkPredecessor() {
		try {
			if (predecessor != null && !predecessor.isAlive())
				predecessor = null;
		} catch (RemoteException e) {
			predecessor = null;
			// e.printStackTrace();
		}
	}

	/**
	 * Constructor for a chord object
	 * 
	 * @param port
	 *            The port number on which the chord will exist
	 * @param guid
	 *            The unique identifier for the node
	 */
	public Chord(int port, long guid) throws RemoteException {
		counter = new Counter();
		mapStruct = new TreeMap<Long, List<String>>();
		reduceStruct = new TreeMap<Long, String>();
		int j;
		finger = new ChordMessageInterface[M];
		for (j = 0; j < M; j++) {
			finger[j] = null;
		}
		this.guid = guid;

		predecessor = null;
		successor = this;
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				stabilize();
				fixFingers();
				checkPredecessor();
			}
		}, 500, 500);
		try {
			// create the registry and bind the name and object.
			System.out.println(guid + " is starting RMI at port=" + port);
			registry = LocateRegistry.createRegistry(port);
			registry.rebind("Chord", this);
		} catch (RemoteException e) {
			throw e;
		}
	}

	/**
	 * Outputs the current state of the current chord ring and finger table
	 */
	void Print() {
		int i;
		try {
			if (successor != null)
				System.out.println("successor " + successor.getId());
			if (predecessor != null)
				System.out.println("predecessor " + predecessor.getId());
			for (i = 0; i < M; i++) {
				try {
					if (finger != null)
						System.out.println("Finger " + i + " " + finger[i].getId());
				} catch (NullPointerException e) {
					finger[i] = null;
				}
			}
		} catch (RemoteException e) {
			System.out.println("Cannot retrive id");
		}
	}
	
	public void emitMap(long key, String value, Counter counter) throws RemoteException{
		if(isKeyInSemiCloseInterval(key, predecessor.getId(), guid)){
			if(mapStruct.get(key) != null){
				mapStruct.get(key).add(value);
			}else{
				List<String> valList = new ArrayList<String>();
				valList.add(value);
				mpaStruct.put(key, valList);
			}
			counter.decrement();
		}
		else if(isKeyInSemiCloseInterval(key, guid, successor.getId())){
			successor.emitMap(key, value counter);
		}else{
			closestPrecedingNode(key).emitMap(key, value, counter);
		}
	}
	
	public void emitReduce(long key, String value, Counter counter) throws RemoteException{
		if(isKeyInSemiCloseInterval(key, predecessor.getId(), guid)){
			reduceStruct.add(key, value);
			counter.decrement();
		}else if(isKeyInSemiCloseInterval(key, guid, successor.getId())){
			successor.emitReduce(key, value, counter);
		}else{
			closestPrecedingNode(key).emitReduce(key, value, counter);
		}
	}
	
	public void mapContext(long page, MapInterface mapper, Counter counter) throws RemoteException{
		File metafile = new File("./" + guid + "/repository/8555781317612585347");
		Thread t = new Thread(new Runnable(){
			//do actions for mapContext here
			//mapper will be done inside a loop
			mapper.map(key, value);
			//counter increment is done after the loop completes
			counter.increment(key, n);
		});
	}
	
	public void reduceContext(long source, ReduceInterface reducer, Counter counter) throws RemoteException{
		if(source != guid){
			counter.add(guid);
			Thread t = new Thread(new Runnable(){
				List<String> keyList = new ArrayList<String>(reduceStruct.keySet());
				List<long> valueList = new ArrayList<long>(reduceStruct.values());
				for(int i=0; i < reduceStruct.size(); i++){
					//iterate through the tree map and execute reducer.reduce
					reducer.reduce(keyList.get(i), valueList.get(i));
				}
			});
			t.start();
			successor.reduceContext(source, reducer, counter);
		}
	}
	
	public void completed(long source, Counter counter) throws RemoteException {
		if(source != guid){
			successor.completed(source, counter);
			counter.increment(guid, 0);
		}
	}
}