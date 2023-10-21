package galactic_messenger_test.responce;

import java.util.HashMap;

public class ResponseParser {
	
	public static HashMap<String, Object> parseResponse(String response) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		short type = -1;
		int delimiter = response.indexOf(" ");
		try {
			type = Short.parseShort(response.substring(0, delimiter));
		} catch (NumberFormatException e) {
			return null;
		}
		
		data.put(ResponseKey.TYPE.key, type);
		
		int i = delimiter+1;
		int fromIndex = i;
		String key = null;
		boolean recordValue = false;
		while(i < response.length()) {
			char c = response.charAt(i);
			if (c == ':' && !recordValue) {
				key = response.substring(fromIndex, i);
			} else if (c == '\"' || i == response.length()-1) {
				if (!recordValue) {
					fromIndex = i;
					recordValue = true;
				} else {
					data.put(key, response.substring(fromIndex+1, i));
					fromIndex = i+2;		
					recordValue = false;
				}
			}
			
			i++;
		}
		
		return data;
	}
	
	public static void main(String[] args) {
		HashMap<String, Object> data = parseResponse("100 CMD:\"abc\" LLL:\"4654\"");
		for (Object key : data.keySet()) {
			System.out.println(key+", "+data.get(key));
		}
	}

}
