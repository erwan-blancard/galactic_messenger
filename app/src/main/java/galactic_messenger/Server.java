package galactic_messenger_test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import galactic_messenger_test.database.DatabaseManager;

public class Server {
	
	private ServerSocket serverSocket;

	public Server(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
		
		DatabaseManager.loadUsers();
	}
	
	public void startServer() {
		System.out.println("Server available at: "+serverSocket.getLocalSocketAddress());
		try {
			
			while (!serverSocket.isClosed()) {
				
				Socket socket = serverSocket.accept();				// blocks the execution
				socket.setSoTimeout(30000);
				System.out.println("A new client has connected: remote address: "+socket.getRemoteSocketAddress());
				ClientHandler clientHandler = new ClientHandler(socket, this);
				
				Thread thread = new Thread(clientHandler);
				thread.start();
				
			}
			
		} catch(IOException e) {}
	}
	
	public void closeServerSocket() {
		
		try {
			if(serverSocket != null) {
				serverSocket.close();
			}
			if (ClientHandler.clientHandlers != null) {
				for (ClientHandler ch : ClientHandler.clientHandlers) {
					ch.closeEverything();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		int port = 0;
		
		// get port argument
		if (args != null && args.length > 0) {
			if (args.length > 1) { System.out.println("Too much arguments.\n\nUsage: java -jar galactic_messenger_server.jar [port | optional]"); System.exit(-1); }
			try {
				port = Integer.parseInt(args[0]);
				if (port > 65535) { throw new NumberFormatException("Number above 65535."); }
				if (port < 0) { throw new NumberFormatException("Number below 0."); }
			} catch (NumberFormatException e) {
				System.out.println("Error: specified port is not a valid number: "+e.getMessage());
				System.exit(-1);
			}
		}
		
		// print warning if port is 0
		if (port == 0) { System.out.println("Warning: no port specified, server will be using an automatically allocated port."); }
		
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(serverSocket == null) { System.out.println("Error: Could not create the ServerSocket !"); System.exit(-1); }
		Server server = new Server(serverSocket);
		server.startServer();
		
	}

}
