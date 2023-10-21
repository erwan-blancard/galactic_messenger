package galactic_messenger_test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

import galactic_messenger_test.responce.ResponseBuilder;
import galactic_messenger_test.responce.ResponseKey;
import galactic_messenger_test.responce.ResponseParser;
import galactic_messenger_test.responce.ResponseType;

public class Client {
	
	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private String username;
	
	private ClientState state = ClientState.WAITING_FOR_LOGIN;
	
	private String privateChatUser = null;
	
	private String currentGroupName = null;
	
	private String serverResponse = null;
	
	private boolean wait = false;
	
	final String[] helpLines = {
			"\nList of commands:\n",
			
			"\t/register \"username\" \"password\"",
			"\t/login \"username\" \"password\"",
			"\t/online_users",
			"\t/help",
			
			"\n\t/private_chat \"username\"",
			"\t/accept \"username\"",
			"\t/decline \"username\"",
			"\t/exit_private_chat",
			
			"\n\t/create_group \"group_name\"",
			"\t/join_group \"group_name\"",
			"\t/exit_group \"group_name\"",
			"\t/msg_group \"group_name\" \"message\"",
			
			"\n\t/upload \"filepath\"",
			"\t/list_files",
			"\t/download \"filename\"",
			
			"\n\t/create_secure_group \"group_name\" \"password\"",
			"\t/join_secure_group \"group_name\" \"password\"",
	};
	
	public Client(Socket socket) {
		try {
			this.socket = socket;
			this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.username = null;
			
		} catch(IOException e) {
			closeEverything();
		}
	}
	
	public void startServerListener() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String response;
				
				while(socket.isConnected()) {
					try {
						response = reader.readLine();
						if (response != null) {
							serverResponse = response;
							// if response is message or group message
							if (response.startsWith(""+ResponseType.MESSAGE.type) || response.startsWith(""+ResponseType.GROUP_MESSAGE.type)) {
								processMessageResponse(response);
							} else {
								wait = false;
							}
						}
					} catch(IOException e) {
						closeEverything();
					}
				}
			}
		}).start();
	}
	
	public void startClient() {
		startServerListener();
		System.out.println("Connected to server.");
		try {
			String request;
			Scanner scanner = new Scanner(System.in);
			while(socket.isConnected()) {
				request = scanner.nextLine();
				processRequest(request);
			}
			scanner.close();
			
		} catch(IOException e) {
			closeEverything();
		}
	}
	
	public void processMessageResponse(String response) {
		try {
			HashMap<String, Object> responseArgs = ResponseParser.parseResponse(response);
			if (responseArgs == null) { System.out.println("Invalid server response (invalid ResponseType)"); return; }
			if (responseArgs.get(ResponseKey.RESPONSE_CONTENT.key) == null) {
				System.out.println("Bad response handling for message / group message.");
				return;
			}
			String message = "";
			if (responseArgs.get(ResponseKey.GROUP.key) != null) {
				message += "["+responseArgs.get(ResponseKey.GROUP.key)+"]";
			}
			message += "["+responseArgs.get(ResponseKey.SENDER.key)+"]: ";
			message += ""+responseArgs.get(ResponseKey.RESPONSE_CONTENT.key);
			System.out.println(message);
		} catch(Exception e) {
			System.out.println("Error in processMessageResponse: "+e.getMessage());
		}
	}
	
	public void processCommandResponse(String request) {
		try {
			String[] requestArgs = request.split(" ");
			String requestCMD = requestArgs[0].substring(1);
			HashMap<String, Object> responseArgs = ResponseParser.parseResponse(serverResponse);
			if (responseArgs == null) { System.out.println("Invalid server response (invalid ResponseType)"); return; }
			if (!requestCMD.equals(responseArgs.get(ResponseKey.COMMAND.key))) {
				System.out.println("Bad response handling for command.");
				return;
			}

			short type = (short) responseArgs.get(ResponseKey.TYPE.key);
			// limit available commands if not logged in
			if (state == ClientState.WAITING_FOR_LOGIN) {
				
				switch (requestCMD) {
				case "register":
					if (type == ResponseType.ACCEPT_REQUEST.type) {
						System.out.println("Account created succesfully ! Use /login to log in.");
					} else if (type == ResponseType.DECLINE_REQUEST.type) {
						System.out.println("Could not register user: "+responseArgs.get(ResponseKey.METADATA.key));
					} else if (type == ResponseType.SERVER_ERROR.type || type == ResponseType.BAD_REQUEST.type) {
						System.out.println("["+type+"] An error occurred.");
					} else {
						System.out.println("["+type+"] Unhandled operation.");
					}
					break;
				case "login":
					if (type == ResponseType.ACCEPT_REQUEST.type) {
						state = ClientState.NOT_IN_CHAT;
						username = (String) responseArgs.get(ResponseKey.METADATA.key);
						System.out.println("Logged in as "+username+".");
					} else if (type == ResponseType.DECLINE_REQUEST.type) {
						System.out.println("Could not log in: "+responseArgs.get(ResponseKey.METADATA.key));
					} else if (type == ResponseType.SERVER_ERROR.type || type == ResponseType.BAD_REQUEST.type) {
						System.out.println("["+type+"] An error occurred.");
					} else {
						System.out.println("["+type+"] Unhandled operation.");
					}
					break;
				}
			} else {
				switch (requestCMD) {
				case "online_users":
					if (type == ResponseType.SERVER_CALLBACK.type) {
						System.out.println(responseArgs.get(ResponseKey.RESPONSE_CONTENT.key));
					} else if (type == ResponseType.BAD_REQUEST.type) {
						System.out.println("Could not show online users list.");
					} else {
						System.out.println("["+type+"] Unhandled operation.");
					}
					break;
				case "private_chat":
					if (type == ResponseType.ACCEPT_REQUEST.type) {
						if (state == ClientState.IN_GROUP_CHAT) {
							state = ClientState.IN_PRIVATE_CHAT_AND_GROUP_CHAT;
						} else if (state == ClientState.IN_SECURE_GROUP) {
							state = ClientState.IN_PRIVATE_CHAT_AND_SECURE_GROUP;
						} else {
							state = ClientState.IN_PRIVATE_CHAT;
						}
						privateChatUser = (String) responseArgs.get(ResponseKey.METADATA.key);
						System.out.println("Invite accepted !");
					} else if (type == ResponseType.DECLINE_REQUEST.type) {
						System.out.println("Invite declined !");
					} else if (type == ResponseType.SERVER_CALLBACK.type) {
						System.out.println(responseArgs.get(ResponseKey.RESPONSE_CONTENT.key));
					} else if (type == ResponseType.SERVER_ERROR.type || type == ResponseType.BAD_REQUEST.type) {
						System.out.println("["+type+"] An error occurred.");
					} else {
						System.out.println("["+type+"] Unhandled operation.");
					}
					break;
				case "accept":
					if (type == ResponseType.ACCEPT_REQUEST.type) {
						System.out.println("Invite accepted.");
					} else if (type == ResponseType.BAD_REQUEST.type) {
						System.out.println("Could not accept request. Check arguments.");
					} else {
						System.out.println("["+type+"] Unhandled operation.");
					}
					break;
				case "decline":
					if (type == ResponseType.ACCEPT_REQUEST.type) {
						System.out.println("Invite declined.");
					} else if (type == ResponseType.BAD_REQUEST.type) {
						System.out.println("Could not decline request. Check arguments.");
					} else {
						System.out.println("["+type+"] Unhandled operation.");
					}
					break;
				case "exit_private_chat":
					if (type == ResponseType.ACCEPT_REQUEST.type) {
						System.out.println("Chat leaved.");
						privateChatUser = null;
						if (state == ClientState.IN_PRIVATE_CHAT_AND_GROUP_CHAT) {
							state = ClientState.IN_GROUP_CHAT;
						} else if (state == ClientState.IN_PRIVATE_CHAT_AND_SECURE_GROUP) {
							state = ClientState.IN_SECURE_GROUP;
						} else {
							state = ClientState.IN_PRIVATE_CHAT;
						}
					} else if (type == ResponseType.SERVER_CALLBACK.type) {
						System.out.println(responseArgs.get(ResponseKey.RESPONSE_CONTENT.key));
					} else if (type == ResponseType.BAD_REQUEST.type) {
						System.out.println("Could not decline request. Check arguments.");
					} else if (type == ResponseType.SERVER_ERROR.type) {
						System.out.println("Chat leaved: the user was not found.");
						privateChatUser = null;
						if (state == ClientState.IN_PRIVATE_CHAT_AND_GROUP_CHAT) {
							state = ClientState.IN_GROUP_CHAT;
						} else if (state == ClientState.IN_PRIVATE_CHAT_AND_SECURE_GROUP) {
							state = ClientState.IN_SECURE_GROUP;
						} else {
							state = ClientState.IN_PRIVATE_CHAT;
						}
					} else {
						System.out.println("["+type+"] Unhandled operation.");
					}
					break;
				case "create_group":
					
					break;
				case "join_group":
					
					break;
				case "exit_group":
					
					break;
				case "msg_group":
					
					break;
				/*
				case "upload":
					
					break;
				case "list_files":
					
					break;
				case "download":
					
					break;
				*/
				/*
				case "create_secure_group":
					
					break;
				case "join_secure_group":
					
					break;
				*/
				}
			}
			
		} catch(Exception e) {
			System.out.println("Error in processCommandResponse: "+e.getMessage());
		}
	}
	
	public void processRequest(String request) throws IOException {
		if (request != null && request.length() > 0) {
			if (request.startsWith("/")) {
				String[] commandArgs = request.split(" ");
				// filter out "/"
				String command = commandArgs[0].substring(1);
				
				if (command.equals("help")) {
					printHelp();
				} else if (state == ClientState.WAITING_FOR_LOGIN) {
					if (command.equals("register") || command.equals("login")) {
						sendRequest(request);
					} else {
						System.out.println("You are not logged in ! Use /login to log in.");
					}
				} else {
					if (!command.equals("register") && !command.equals("login")) {
						sendRequest(request);
					} else {
						System.out.println("You are already logged in !");
					}
				}
			} else if (state == ClientState.IN_PRIVATE_CHAT || state == ClientState.IN_PRIVATE_CHAT_AND_GROUP_CHAT || state == ClientState.IN_PRIVATE_CHAT_AND_SECURE_GROUP) {
				// if request is a private message
				sendRequest(request);
			} else if (state != ClientState.WAITING_FOR_LOGIN){
				System.out.println("You are not in a Private Chat ! Use /private_chat to join one.");
			} else {
				System.out.println("You are not logged in ! Use /login to log in.");
			}
		}
	}
	
	public void sendRequest(String request) throws IOException {
		writer.write(request);
		writer.newLine();
		writer.flush();
		
		// if command
		if (request.startsWith("/") && !request.startsWith("/msg_group")) {
			while (wait) {} // block until server response
			try {
				Thread.sleep(100); // temp sync fix
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processCommandResponse(request);
		}
	}
	
	public boolean isLoggedIn() {
		return username != null && state != ClientState.WAITING_FOR_LOGIN;
	}

	public void closeEverything() {
		try {
			if(reader != null) {
				reader.close();
			}
			if(writer != null) {
				writer.close();
			}
			if(socket != null) {
				socket.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printHelp() {
		for (String line : helpLines) { System.out.println(line); }
	}
	
	public static void main(String[] args) {
		
		String ip = null;
		int port = -1;
		
		// get ip and port arguments
		if (args != null && args.length > 0) {
			if (args.length > 2) { System.out.println("Too much arguments.\n\nUsage: java -jar galactic_messenger_client.jar [ip_address] [port]"); System.exit(-1); }
			if (args.length < 2) { System.out.println("Too few arguments.\n\nUsage: java -jar galactic_messenger_client.jar [ip_address] [port]"); System.exit(-1); }
			ip = args[0];
			try {
				port = Integer.parseInt(args[1]);
				if (port > 65535) { throw new NumberFormatException("Number is above 65535."); }
				if (port <= 0) { throw new NumberFormatException("Number is below or equal to 0."); }
			} catch (NumberFormatException e) {
				System.out.println("Error: specified port is not a valid number: "+e.getMessage());
				System.exit(-1);
			}
		} else {
			System.out.println("Usage: java -jar galactic_messenger_client.jar [ip_address] [port]");
			System.exit(-1);
		}
		
		Socket socket = null;
		try {
			socket = new Socket(ip, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(socket == null) { System.out.println("Socket is null!\tExiting..."); System.exit(-1); }
		Client client = new Client(socket);
		client.startClient();
		
	}

}
