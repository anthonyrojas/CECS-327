import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;

/**
 * This class is a mapper and reducer for the map reduce algorithm
 * */
public class Mapper implements MapInterface, ReduceInterface, Serializable
{
	/**
	 * The chord associated with this mapper
	 * */
	ChordMessageInterface chord;

	/**
	 * The counter associated with this mapper
	 * */
	CounterInterface counter;

	/**
	 * Constructor that initializes a new Mapper object
	 * @param c The chord used with this mapper
	 * @param cont The counter with mapper
	 * */
	public Mapper(ChordMessageInterface c, CounterInterface cont){
		this.chord = c;
		this.counter = cont;
	}

	/**
	 * Hashes the objectName to create a unique guid
	 *
	 * @param objectName
	 *            name of the object or chord or peer
	 * @return guid of the object using md5 hashing
	 */
	private long md5(String objectName) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(objectName.getBytes());
			BigInteger bigInt = new BigInteger(1, m.digest());
			return Math.abs(bigInt.longValue());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Maps each word to a guid and inserts them into a map structure
	 * @param key The guid of the page from which the words are coming from.
	 * @param value The words from the original page file
	 * */
	public void map(long key, String value) throws IOException
	{
		String[] words = value.split("\\s+");
		for(String word : words){
			long wordGuid = md5(word);
			chord.emitMap(wordGuid, word + ":" + 1, counter);
		}
	}

	/**
	 * Reduces the map structure by counting the words
	 * @param key The md5 value of the word
	 * @param values An array of the same words
	 * */
	public void reduce(long key, String values[]) throws IOException {
		String word = values[0].split(":")[0];
		chord.emitReduce(key, word + ":" + values.length, counter);
	}
}