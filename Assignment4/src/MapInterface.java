import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * This class is the interface that lays out the methods associated with a mapper in map reduce
 * */
public interface MapInterface {
	/**
	 * Maps each word to a guid and inserts them into a map structure
	 * @param key The guid of the page from which the words are coming from.
	 * @param value The words from the original page file
	 * */
	public void map(long key, String value) throws IOException;
}