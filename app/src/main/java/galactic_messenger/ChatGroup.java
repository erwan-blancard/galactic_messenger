package galactic_messenger_test;

import java.util.Vector;

public class ChatGroup {
	
	public static Vector<ChatGroup> groups = new Vector<ChatGroup>();
	
	private String groupName;
	private Vector<String> members;
	
	public ChatGroup(String groupName) {
		this.groupName = groupName;
		this.members = new Vector<String>();
		groups.add(this);
	}
	
	public String getName() {
		return groupName;
	}
	
	public void addUserToGroup(String username) {
		if (!members.contains(username)) {
			members.add(username);
		}
	}
	
	public void removeUserFromGroup(String username) {
		int index = members.indexOf(username);
		if (index != -1) {
			members.remove(index);
		}
	}
	
	public boolean hasMember(String username) {
		return members.contains(username);
	}
	
	public String[] getMembers() {
		return (String[]) members.toArray();
	}

}
