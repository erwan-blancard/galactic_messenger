package galactic_messenger_test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import galactic_messenger_test.auth.UserAuthentication;
import galactic_messenger_test.database.DatabaseManager;
import galactic_messenger_test.responce.ResponseBuilder;

public class ClientHandler implements Runnable {

	public static Vector<ClientHandler> clientHandlers = new Vector<ClientHandler>();
	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	
	private Server server;
	
	// client relative fields
	private User user = null;
	private ClientState state = ClientState.WAITING_FOR_LOGIN;
	
	private User privateChatUser = null;
	private Vector<User> pendingInvites = new Vector<User>();
	
	private ChatGroup group = null;
	
	public ClientHandler(Socket socket, Server server) {
		try {
			this.socket = socket;
			this.server = server;
			this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			clientHandlers.add(this);
			// broadcastMessage("SERVER: " + clientUsername + " has joined");
			
		} catch(IOException e) {
			closeEverything();
		}
		
	}
	
	@Override
	public void run() {
		String clientRequest;
		
		while(socket.isConnected()) {
			
			try {
				clientRequest = reader.readLine();
				if (clientRequest != null) {
					System.out.println(((user != null) ? user.getName() : socket.getLocalSocketAddress())+": "+clientRequest);
					processRequest(clientRequest.strip().split(" "));
				}
			} catch(IOException e) {
				closeEverything();
				break;
			}
			
		}
	}
	/*
	private void handleJoinSecureGroup(String[] requestArgs) {
		
	}

	private void handleCreateSecureGroup(String[] requestArgs) {
		
	}
	*/
	/*
	private void handleFileDownload(String[] requestArgs) {
		
	}

	private void handleShowListOfFiles(String[] requestArgs) {
		
	}

	private void handleFileUpload(String[] requestArgs) {
		
	}
	*/

	private void handleSendMessageGroup(String[] requestArgs) {
		
	}

	private void handleExitGroup(String[] requestArgs) {
		
	}

	private void handleJoinGroup(String[] requestArgs) {
		
	}

	private void handleCreateGroup(String[] requestArgs) {
		
	}

	private void handlePrivateChatExit(String[] requestArgs) {
		if (requestArgs.length != 1) {
			sendDataToClient(ResponseBuilder.buildBadResponse(requestArgs[0]));
			return;
		}
		
		if (privateChatUser == null) {
			sendDataToClient(ResponseBuilder.buildServerCallback(requestArgs[0], "You are not in a private chat."));
			return;
		}
		
		for (ClientHandler ch : clientHandlers) {
			if (ch.user.getName().equals(privateChatUser.getName())) {
				ch.sendDataToClient(ResponseBuilder.buildMessageResponse(user.getName()+" leaved the chat.", "SERVER"));
				privateChatUser = null;
				sendDataToClient(ResponseBuilder.buildAcceptResponse(requestArgs[0], "OK"));
				return;
			}
		}
		// if no user is found, send a server error
		sendDataToClient(ResponseBuilder.buildServerErrorResponse(requestArgs[0]));
	}

	private void handlePrivateChatDecline(String[] requestArgs) {
		if (requestArgs.length != 2) {
			sendDataToClient(ResponseBuilder.buildBadResponse(requestArgs[0]));
			return;
		}
		
		if (pendingInvites.isEmpty()) {
			sendDataToClient(ResponseBuilder.buildServerCallback(requestArgs[0], "You have no pending invites."));
			return;
		}
		
		String user = requestArgs[1];
		for (User userInInvite : pendingInvites) {
			if (userInInvite.getName().equals(user)) {
				for (ClientHandler ch : clientHandlers) {
					if (ch.user.getName().equals(user)) {
						pendingInvites.remove(ch.user);
						ch.sendDataToClient(ResponseBuilder.buildDeclineResponse("private_chat", requestArgs[1]));
						sendDataToClient(ResponseBuilder.buildAcceptResponse(requestArgs[0], requestArgs[1]));
						return;
					}
				}
				sendDataToClient(ResponseBuilder.buildServerCallback(requestArgs[0], "This user didn't sent you a private chat request."));
			}
		}
	}

	private void handlePrivateChatAccept(String[] requestArgs) {
		if (requestArgs.length != 2) {
			sendDataToClient(ResponseBuilder.buildBadResponse(requestArgs[0]));
			return;
		}
		
		if (pendingInvites.isEmpty()) {
			sendDataToClient(ResponseBuilder.buildServerCallback(requestArgs[0], "You have no pending invites."));
			return;
		}
		
		String user = requestArgs[1];
		for (User userInInvite : pendingInvites) {
			if (userInInvite.getName().equals(user)) {
				for (ClientHandler ch : clientHandlers) {
					if (ch.user.getName().equals(user)) {
						pendingInvites.remove(ch.user);
						ch.sendDataToClient(ResponseBuilder.buildAcceptResponse("private_chat", requestArgs[1]));
						sendDataToClient(ResponseBuilder.buildAcceptResponse(requestArgs[0], requestArgs[1]));
						return;
					}
				}
				sendDataToClient(ResponseBuilder.buildServerCallback(requestArgs[0], "This user didn't sent you a private chat request."));
			}
		}
	}

	private void handleCreatePrivateChat(String[] requestArgs) {
		if (requestArgs.length != 2) {
			sendDataToClient(ResponseBuilder.buildBadResponse(requestArgs[0]));
			return;
		}
		
		String user = requestArgs[1];
		if (User.isUserOnline(user)) {
			for (ClientHandler ch : clientHandlers) {
				if (ch.user.getName().equals(user)) {
					ch.pendingInvites.add(this.user);
					ch.sendDataToClient(ResponseBuilder.buildMessageResponse(this.user.getName()+" wants to start a private chat! Use /accept [user] or /decline [user] to answer.", "SERVER"));
					break;
				}
			}
		} else {
			sendDataToClient(ResponseBuilder.buildServerCallback(requestArgs[0], "This user is not online !"));
		}
	}

	private void showOnlineUsersList(String[] requestArgs) {
		List<String> users = new ArrayList<String>();
		for (ClientHandler ch : clientHandlers) {
			if (!ch.user.getName().equals(user.getName())) {
				users.add(ch.user.getName());
			}
		}
		String data = "No users online.";
		if (!users.isEmpty()) {
			String users_list_str = users.toString();
			data = users_list_str.substring(1, users_list_str.length()-1);
		}
		// send list -> user1, user2, etc
		sendDataToClient(ResponseBuilder.buildServerCallback(requestArgs[0], data));
	}

	private void handleLogin(String[] requestArgs) {
		if (requestArgs.length != 3) {
			sendDataToClient(ResponseBuilder.buildBadResponse(requestArgs[0]));
			return;
		}
		
		if (User.isUserOnline(requestArgs[1])) {
			sendDataToClient(ResponseBuilder.buildDeclineResponse(requestArgs[0], "User is already online."));
			return;
		}
		
		if (UserAuthentication.authenticate(requestArgs[1], requestArgs[2])) {
			user = new User(requestArgs[1]);
			state = ClientState.NOT_IN_CHAT;
			sendDataToClient(ResponseBuilder.buildAcceptResponse(requestArgs[0], requestArgs[1]));	// TODO: implement token system?
		} else {
			sendDataToClient(ResponseBuilder.buildDeclineResponse(requestArgs[0], "Username or Password is invalid."));
		}
	}

	private void handleRegister(String[] requestArgs) {
		if (requestArgs.length != 3) {
			sendDataToClient(ResponseBuilder.buildBadResponse(requestArgs[0]));
			return;
		}
		
		if (!DatabaseManager.hasUser(requestArgs[1])) {
			if (DatabaseManager.registerUser(requestArgs[1], requestArgs[2])) {
				sendDataToClient(ResponseBuilder.buildAcceptResponse(requestArgs[0], "OK"));
			} else {
				sendDataToClient(ResponseBuilder.buildBadResponse(requestArgs[0]));
			}
		} else {
			sendDataToClient(ResponseBuilder.buildDeclineResponse(requestArgs[0], "User already exists."));
		}
	}

	private void processRequest(String[] requestArgs) {
		if (requestArgs.length <= 0) { System.out.println("Empty request from user"+((user != null) ? user.getName() : socket.getLocalSocketAddress())+", return."); return; }
		
		String command = requestArgs[0];
		
		// if request is command
		if (command.startsWith("/")) {
			// filter out "/" from command
			command = command.substring(1);
			requestArgs[0] = command;
			
			try {
				// limit available commands if not logged in
				if (state == ClientState.WAITING_FOR_LOGIN) {
					switch (command) {
					case "register":
						handleRegister(requestArgs);
						break;
					case "login":
						handleLogin(requestArgs);
						break;

					default:
						sendDataToClient(ResponseBuilder.buildServerCallback(command, "You cannot perform this action, you are not logged in ! To log in, use the /login command."));
						break;
					}
				} else {
					switch (command) {
					case "online_users":
						if (requestArgs.length > 1) {
							sendDataToClient(ResponseBuilder.buildBadResponse("online_users"));
						} else {
							showOnlineUsersList(requestArgs);
						}
						break;
					case "register":
						sendDataToClient(ResponseBuilder.buildServerCallback(command, "You cannot perform this action, you are already logged in !"));
						break;
					case "login":
						sendDataToClient(ResponseBuilder.buildServerCallback(command, "You cannot perform this action, you are already logged in !"));
						break;
					case "private_chat":
						handleCreatePrivateChat(requestArgs);
						break;
					case "accept":
						handlePrivateChatAccept(requestArgs);
						break;
					case "decline":
						handlePrivateChatDecline(requestArgs);
						break;
					case "exit_private_chat":
						handlePrivateChatExit(requestArgs);
						break;
					case "create_group":
						handleCreateGroup(requestArgs);
						break;
					case "join_group":
						handleJoinGroup(requestArgs);
						break;
					case "exit_group":
						handleExitGroup(requestArgs);
						break;
					case "msg_group":
						handleSendMessageGroup(requestArgs);
						break;
					/*
					case "upload":
						handleFileUpload(requestArgs);
						break;
					case "list_files":
						handleShowListOfFiles(requestArgs);
						break;
					case "download":
						handleFileDownload(requestArgs);
						break;
					*/
					/*
					case "create_secure_group":
						handleCreateSecureGroup(requestArgs);
						break;
					case "join_secure_group":
						handleJoinSecureGroup(requestArgs);
						break;
					*/

					default:
						sendDataToClient(ResponseBuilder.buildBadResponse(command));
						break;
					}
				}
			} catch(Exception e) {
				System.err.println("Error while processing command "+command+"!");
				e.printStackTrace();
				sendDataToClient(ResponseBuilder.buildServerErrorResponse(command));
			}
		}
	}

	public void sendDataToClient(String data) {
		try {
			writer.write(data);
			writer.newLine();
			writer.flush();
		} catch(IOException e) {
			closeEverything();
		}
	}
	
	public boolean isUserOnlineAndAvailable() {
        return socket.isConnected() && !socket.isClosed();
    }

	public void broadcastMessage(String messageToSend) {
		for(ClientHandler clientHandler : clientHandlers) {
			if(!clientHandler.user.getName().equals(user.getName())) {
				clientHandler.sendDataToClient(messageToSend);
			}
		}
	}
	
	public void removeClientHandler() {
		clientHandlers.remove(this);
		broadcastMessage("SERVER: " + user.getName() + " has left");
	}
	
	public void closeEverything() {
		removeClientHandler();
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

}
