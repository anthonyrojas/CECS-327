import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * This class is the counter for words in a file
 * */
public class Counter implements CounterInterface, Serializable{
	/**
	 * The number of rows
	 * */
	Long counter = 0L;

	/**
	 * The set of guids for each row
	 * */
	Set<Long> set;

	/**
	 * Initializes a new Counter object
	 * */
	public Counter(){
		set = new HashSet<Long>();
	}

	/**
	 * Adds a key to the set of guids
	 * @param key The key to be added to the set
	 * */
	public void add(long key) {
		set.add(key);
	}

	/**
	 * Checks if the Counter is reset back to default values
	 * @return If the Counter is empty and reset
	 * */
	public Boolean hasCompleted() {
		if (counter <= 0 && set.isEmpty())
			return true;
		return false;
	}

	/**
	 * Increments the counter and removes the key from the set of keys
	 * @param key The md5 value of the word
	 * @param n The number of rows
	 * */
	public void increment(long key, long n) throws RemoteException {
		set.remove(key);
		counter += n;
	}

	/**
	 * Decreased the counter number of rows
	 * */
	public void decrement() throws RemoteException {
		counter--;
	}
}
