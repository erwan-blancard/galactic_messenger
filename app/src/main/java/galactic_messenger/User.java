package galactic_messenger_test;

import java.util.Vector;

public class User {
	
	public static Vector<User> onlineUsers = new Vector<User>();
	private String name;
	
	public User(String name) {
		this.name = name;
		onlineUsers.add(this);
	}
	
	public String getName() {
		return name;
	}
	
	public static boolean isUserOnline(String username) {
		for (User user : onlineUsers) {
			if (user.getName().equals(username)) {
				return true;
			}
		}
		return false;
	}

}
