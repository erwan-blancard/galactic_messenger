package galactic_messenger_test.responce;

import galactic_messenger_test.ChatGroup;

public class ResponseBuilder {
	
	public static String strSet(ResponseKey key, String value) { return key.key+":\""+value+"\""; }
	
	
	public static String buildResponse(ResponseType type, String[] datalist) {
		String joined_data = " ";
		for (int i = 0; i < datalist.length; i++) {
			joined_data += datalist[i] + ((i == datalist.length-1) ? "" : " ");
		}
		return type.type + joined_data;
		
	}
	
	public static String buildServerCallback(String command, String message) {
		String[] datalist = {strSet(ResponseKey.COMMAND, command), strSet(ResponseKey.RESPONSE_CONTENT, message)};
		return buildResponse(ResponseType.SERVER_CALLBACK, datalist);
	}
	
	public static String buildBadResponse(String command) {
		String[] datalist = {strSet(ResponseKey.COMMAND, command)};
		return buildResponse(ResponseType.BAD_REQUEST, datalist);
	}
	
	public static String buildServerErrorResponse(String command) {
		String[] datalist = {strSet(ResponseKey.COMMAND, command)};
		return buildResponse(ResponseType.SERVER_ERROR, datalist);
	}
	
	public static String buildAcceptResponse(String command, String metadata) {
		String[] datalist = {strSet(ResponseKey.COMMAND, command), strSet(ResponseKey.METADATA, metadata)};
		return buildResponse(ResponseType.ACCEPT_REQUEST, datalist);
	}
	
	public static String buildDeclineResponse(String command, String metadata) {
		String[] datalist = {strSet(ResponseKey.COMMAND, command), strSet(ResponseKey.METADATA, metadata)};
		return buildResponse(ResponseType.ACCEPT_REQUEST, datalist);
	}
	
	public static String buildMessageResponse(String message, String sender) {
		String[] datalist = {strSet(ResponseKey.RESPONSE_CONTENT, message), strSet(ResponseKey.SENDER, sender)};
		return buildResponse(ResponseType.MESSAGE, datalist);
	}
	
	public static String buildGroupMessageResponse(String message, String sender, ChatGroup group) {
		String[] datalist = {strSet(ResponseKey.RESPONSE_CONTENT, message), strSet(ResponseKey.SENDER, sender), strSet(ResponseKey.GROUP, group.getName())};
		return buildResponse(ResponseType.GROUP_MESSAGE, datalist);
	}

}
