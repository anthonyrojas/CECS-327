import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

public interface CounterInterface extends Remote {
	public void add(Long key) throws RemoteException;

	public void increment(long key, long n) throws RemoteException;

	public void decrement() throws RemoteException;
}
