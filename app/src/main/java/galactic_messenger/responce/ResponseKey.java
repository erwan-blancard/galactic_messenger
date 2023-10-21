package galactic_messenger_test.responce;

public enum ResponseKey {
	
	// key identifiers
	TYPE("TYPE"),
	COMMAND("CMD"),
	METADATA("META"),
	RESPONSE_CONTENT("CONTENT"),
	SENDER("SENDER"),
	GROUP("GROUP"),
	;
	
	public String key;
	
	ResponseKey(String s) {
		key = s;
	}

}
