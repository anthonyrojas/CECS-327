import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * This class is the interface that lays out the methods for a reducer in a map reduce
 * */
public interface ReduceInterface {
	/**
	 * Reduces the map structure by counting the words
	 * @param key The md5 value of the word
	 * @param value An array of the same words
	 * */
	public void reduce(long key, String value[]) throws IOException;
}