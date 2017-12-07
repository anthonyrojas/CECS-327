import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * This class is the interface that lays out methods for the Counter
 * */
public interface CounterInterface extends Remote {
	/**
	 * Adds a key to the counter set
	 * @param key The key of the word
	 * */
	public void add(long key) throws RemoteException;

	/**
	 * Increments the counter
	 * @param key The key for the row in the counter
	 * @param n Number of rows
	 * */
	public void increment(long key, long n) throws RemoteException;

	/**
	 * Decrements the counter
	 * */
	public void decrement() throws RemoteException;

	/**
	 * Checks if the counter has been completed
	 * @return The boolean value of whether of not the set is empty
	 * */
	public Boolean hasCompleted();
}
