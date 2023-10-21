package galactic_messenger_test.responce;

public enum ResponseType {
	
	// for command errors (invalid command, args, etc)
	BAD_REQUEST(40),
	// for server messages (online users list, etc)
	SERVER_CALLBACK(100),
	// for server errors when processing commands
	SERVER_ERROR(80),
	// to notify the user that a private chat request was accepted
	ACCEPT_REQUEST(20),
	// to notify the user that a private chat request was declined
	DECLINE_REQUEST(30),
	// for messages
	MESSAGE(10),
	// for group messages
	GROUP_MESSAGE(50),
	// ping
	PING(200),
	;
	
	public short type;
	
	ResponseType(int i) {
		this.type = (short) i;
	}

}
