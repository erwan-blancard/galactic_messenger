package galactic_messenger_test;

public enum ClientState {
	
	WAITING_FOR_LOGIN(0),
	NOT_IN_CHAT(1),
	IN_PRIVATE_CHAT(2),
	IN_GROUP_CHAT(3),
	IN_SECURE_GROUP(4),
	IN_PRIVATE_CHAT_AND_GROUP_CHAT(5),
	IN_PRIVATE_CHAT_AND_SECURE_GROUP(6),
	;
	
	int id;
	
	ClientState(int id) {
		this.id = id;
	}

}
