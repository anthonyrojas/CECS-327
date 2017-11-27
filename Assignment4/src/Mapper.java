import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class Mapper implements MapInterface, ReduceInterface
{

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

	public void map(long key, String value) throws IOException
	{
		//fore each word in value
		//emit(md5(word), word + ":" + 1);
		String[] words = value.split("\\s+");
		for(int i=0; i < words.length; i++){
			emit(md5(words[i]), words[i] + ":" + 1);
		}
	}
	
	public void emit(long key, String value){
		
	}

	public void reduce(long key, String values[]) throws IOException {
		String word = values[0].split(":")[0];
		emit(key, word + ":" + values.length);
		//emit(key, word + ":" + values.length);
	}
}