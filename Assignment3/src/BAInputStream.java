import java.io.*;
import java.nio.*;

public class BAInputStream extends InputStream implements Serializable{
	//ByteArrayInputStream b;
	private byte[] data;
	private int count;
	private int mark;
	private int pos;
	public BAInputStream(byte[] d){
		//b = new ByteArrayInputStream(d);
		data = d;
		count = data.length;
		pos = 0;
	}
	
	public synchronized int read(){
		if(pos < count){
			return data[pos++] & 0xff;
		}
		return -1;
	}
	
	public synchronized int available(){
		return count - pos;
	}
}
