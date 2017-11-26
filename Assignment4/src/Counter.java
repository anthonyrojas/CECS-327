import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class Counter implements CounterInterface {
	Long counter = 0L;
	Set<Long> set;

	public void add(Long key) {
		set.add(key);
	}

	public Boolean hasCompleted() {
		if (counter == 0 && set.isEmpty())
			return true;
		return false;
	}

	public void increment(long key, long n) throws RemoteException {
		set.remove(key);
		counter += n;
	}

	public void decrement() throws RemoteException {
		counter--;
	}

	public Counter{
		set = new Set<long>();
	}
}
