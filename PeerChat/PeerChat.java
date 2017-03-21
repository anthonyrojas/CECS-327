import java.net.*;
import java.io.*;
import java.util.*;

/*****************************/
/**
 * \brief It implements a distributed chat. It creates a ring and delivers
 * messages using flooding
 **********************************/
public class PeerChat implements Serializable {
	public static int predPort;// port before me
	public static int succPort;// port after me

	public enum enum_MSG {
		JOIN, // 0
		ACCEPT, // 1
		LEAVE, // 2
		PUT, // 3
	};

	public class Message implements Serializable {
		public enum_MSG msgid;
		public int portSrc;
		public int portDest;
		public String text;
		public Boolean fromInput;
	}

	/*****************************/
	/**
	 * \class Server class "chat.java" \brief It implements the server
	 **********************************/
	private class Server implements Runnable {
		int intialPort;

		// NOTE: control the connections to the ports in the server class.
		// this why you have the void "run" method.
		// String id;
		public Server(int p) {// Server takes a port only
			intialPort = p;
		}

		/*****************************/
		/**
		 * \class Message class "chat.java" \brief JOIN: id, port
		 **********************************/
		public void joinAnotherServer(int port) {
			// take any number not currently assigned to me, and assign it to my
			// successor.
			// I want to wrap this in a try/catch block.
			if (succPort == port) {
				System.out.println("Already, connected, can not connect to self again.");
			} else {
				succPort = port;// next
			}
		}

		/*****************************/
		/**
		 * \class Message class "chat.java" \brief ACCEPT: Id_pred, Port_pred,
		 * IP_pred
		 **********************************/
		public void acceptAnotherServer(int port) {
			// take any number not currently assigned to me, and assign it to my
			// successor.
			// I want to wrap this in a try/catch block.
			if (predPort == port) {
				System.out.println("Already, connected, can not connect to self again.");
			} else {
				predPort = port;
			}
		}

		public void sendMsgToNode(Message m, int toPort) {
			try {
				System.out.println("[Send MSG] Sending message to port:" + toPort);
				Socket socket = new Socket("localhost", toPort);
				System.out.println("[Client] Just connected to " + socket.getRemoteSocketAddress());
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(m);
			} catch (SocketException e) {
				System.out.println("[Send MSG] User not availible");
			} catch (IOException e) {
				System.out.println("[Send MSG] IO: " + e.getMessage());
				e.printStackTrace();
			}
		}

		public void run() {
			/*****************************//**
											 * \brief It allows the system to
											 * interact with the participants.
											 **********************************/
			try {
				ServerSocket servSock = new ServerSocket(intialPort);// create
																		// the
																		// server
																		// off
																		// port
				System.out.println("Waiting for client on port " + servSock.getLocalPort() + "...");
				while (true) {
					Socket clntSock = servSock.accept(); // .accept() returns a
															// socket object
					System.out.println("[Server] Just connected to " + clntSock.getRemoteSocketAddress());
					ObjectInputStream ois = new ObjectInputStream(clntSock.getInputStream());
					ObjectOutputStream oos = new ObjectOutputStream(clntSock.getOutputStream());
					try {
						Message m = (Message) ois.readObject();// not sure
																// what's going
																// on here
																// (magic).
						/// PUT
						if (m.msgid == enum_MSG.PUT) {// if message is PUT
							if (m.portDest == intialPort) {// AND its meant for
															// me
								System.out.println(m.text + " portSrc:" + m.portSrc);
							} else {
								sendMsgToNode(m, succPort);
							}
						}
						// LEAVE
						if (m.msgid == enum_MSG.LEAVE) {
							if (m.fromInput == true) {
								m.text = predPort + " " + succPort;
								m.fromInput = false;
								System.out.println("Node:" + intialPort + " exiting");
								System.exit(0);
							} else {
								System.out.println("[Leave msg]: " + m.text);
								List<Integer> list = new ArrayList<Integer>();
								for (String s : m.text.split("\\s")) {
									list.add(Integer.parseInt(s));
								}
								list.toArray();
								if (list.get(0) == intialPort) {// if im pred
									succPort = list.get(1);// my succ == leaving
															// node's succ
								}
								if (list.get(1) == intialPort) {// if im succ
									predPort = intialPort;
								}
							}
						}
						/// JOIN
						if (m.msgid == enum_MSG.JOIN) {// if message is JOIN
							if (m.fromInput == true) {
								m.fromInput = false;
								joinAnotherServer(m.portDest);
								sendMsgToNode(m, m.portDest);// i don't think
																// this should
																// be here.
							} else {// from someone else
								predPort = m.portSrc;
							}
						}
						System.out.println(predPort + "--->" + "[" + intialPort + "] " + "--->" + succPort);
					} catch (ClassNotFoundException e) {
						System.out.println("[Server] IO Class: " + e.getMessage());
					}
				}
			} catch (SocketException e) {
				System.out.println("[Server] Socket: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("[Server] IO: " + e.getMessage());
			}
		}
	}

	/*****************************//*
									 * \brief It implements the client
									 **********************************/
	private class Client implements Runnable {
		String id;
		int port;

		public Client(String id, int p) {
			System.out.println("The Client was created on port: " + p + " with Id: " + id);
			port = p;
			id = id;
		}

		/*****************************/
		/**
		 * \brief It allows the user to interact with the system.
		 **********************************/
		public void run() {
			boolean stay=true;
			while (true) {
				try {
					Socket socket = new Socket(id, port);
					System.out.println("[Client] Just connected to " + socket.getRemoteSocketAddress());
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					System.out.println(
							"Please enter a the following option,\n 1)join <port>\n 2)put <destination port> <message>\n 3)Leave\n Note: press ENTER to see nodes predessor and successor\n");
					String input = System.console().readLine();
					// split input by spaces
					List<String> list = new ArrayList<String>();
					for (String s : input.split("\\s")) {
						list.add(s);
					}
					list.toArray();
					if (list.contains("put")) {// send msg from clint(input) to
												// Node server
						Message m = new Message();
						m.text = list.get(2);
						m.fromInput = true;
						m.msgid = enum_MSG.PUT;
						m.portDest = Integer.parseInt(list.get(1));
						m.portSrc = port;
						oos.writeObject(m);// send msg to my node
					}
					if (list.contains("join")) {
						System.out.println("joining!");
						Message m = new Message();
						m.fromInput = true;
						m.msgid = enum_MSG.JOIN;
						m.portDest = Integer.parseInt(list.get(1));
						m.portSrc = port;
						oos.writeObject(m);// send msg to my node
					}
					if (list.contains("leave")) {
						System.out.println("im leaving!!");
						Message m = new Message();
						m.fromInput = true;
						m.msgid = enum_MSG.LEAVE;
						m.portSrc = port;
						oos.writeObject(m);// send msg to my node
					} else {
						Message m = new Message();
						m.fromInput = true;
						oos.writeObject(m);
					}
				} catch (SocketException e) {
					System.out.println("[Client] Socket: " + e.getMessage());
				} catch (IOException e) {
					System.out.println("[Client] IO: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * NOTE: This is the first method that gets called when the main method is
	 * called. The "localhost" and the "8000" or any string : number combination
	 * will give the number of the port.
	 */
	public PeerChat(String idThing, int port) {// for example: localhost 8000
		predPort = port;
		succPort = port;
		Thread server = new Thread(new Server(port));// 8000
		Thread client = new Thread(new Client(idThing, port)); // Localhost,
																// 8000
		server.start();
		client.start();
		try {
			client.join();// not our server join methods.
			server.join();
		} catch (InterruptedException e) {
			System.out.println("Thread: " + e.getMessage());
		}
	}

	/*****************************/
	/**
	 * Starts the threads with the client and server: \param Id unique
	 * identifier of the process \param port where the server will listen
	 **********************************/
	public static void main(String [] args) {
		if (args.length < 2) {
			throw new IllegalArgumentException("Parameter: <id> <port>");
		}
		PeerChat chat = new PeerChat(args[0], Integer.parseInt(args[1]));
	}
}
