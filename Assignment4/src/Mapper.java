import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class Mapper implements MapInterface, ReduceInterface
{

	public void map(long key, String value) throws IOException
	{
		//fore each word in value
		//emit(md5(word), word + ":" + 1);
	}
	
	public void emit(){
		
	}

	public void reduce(long key, String values[]) throws IOException {
		String word = values[0].split(":")[0];
		//emit(key, word + ":" + values.length);
	}
}