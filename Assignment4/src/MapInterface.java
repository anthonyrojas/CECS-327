import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

public interface MapInterface {
	public void map(long key, String value) throws IOException;
}