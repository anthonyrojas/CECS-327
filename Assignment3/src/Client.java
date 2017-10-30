import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.nio.file.*;

public class Client implements Serializable {
	DFS dfs;

	public Client(int p) throws Exception {
		dfs = new DFS(p);
		showMenu(dfs);
		// User interface:
		// join, ls, touch, delete, read, tail, head, append, move
	}
	
	public void showMenu(DFS dfs) throws Exception{
		Scanner in = new Scanner(System.in);
		String command = "";
		String commands = " join, ls, touch, delete, read, tail, head, append, move, quit";
		String fileName;
		int pageNumber;
		while(command != null)
		{
			System.out.println("Enter a command: " + commands);
			command = in.next();
			try{
				switch(command.toLowerCase())
				{
					case "join": System.out.println("Join selected");
						String ip = "";
						int p;
						System.out.println("Enter the ip: ");
						ip = in.next();
						System.out.println("Enter the port: ");
						p = in.nextInt();
						dfs.join(ip, p);
						break;
					case "ls" : System.out.println("LS selected");
						System.out.println(dfs.ls());
						break;
					case "touch": System.out.println("Touch selected");
						System.out.println("Enter the new file name: ");
						String filename = in.next();
						dfs.touch(filename);
						break;
					case "delete": System.out.println("Delete selected");
						System.out.println("Enter the file name you want to delete: ");
						fileName = in.next();
						dfs.delete(fileName);
						break;
					case "read": System.out.println("Read selected");
						System.out.println("Enter the file name: ");
						fileName = in.next();
						System.out.println("Enter the page number: ");
						pageNumber = in.nextInt();
						byte[] readBytes = dfs.read(fileName, pageNumber);
						System.out.println(new String(readBytes));
						break;
					case "tail": System.out.println("Tail selected");
						System.out.println("Enter the file name: ");
						fileName = in.next();
						byte[] tailBytes = dfs.tail(fileName);
						System.out.println(new String(tailBytes));
						break;
					case "head": System.out.println("Head selected");
						System.out.println("Enter the file name: ");
						fileName = in.next();
						byte[] headBytes = dfs.head(fileName);
						System.out.println(new String(headBytes));
						break;
					case "append": System.out.println("Append selected");
						break;
					case "move" : System.out.println("Move selected");
						System.out.println("Enter the current file name: ");
						String oldName = in.next();
						System.out.println("Enter the new file name: ");
						String newName = in.next();
						dfs.mv(oldName, newName);
						break;
					case "quit": System.exit(0);
						break;
				default: System.out.println("Invalid input");
				}	
			}catch(Exception e){
				System.out.println(e.toString());
			}
		}
	}
	
	static public void main(String args[]) throws Exception {
		if (args.length < 1) {
			throw new IllegalArgumentException("Parameter: <port>");
		}
		Client client = new Client(Integer.parseInt(args[0]));
	}
}
