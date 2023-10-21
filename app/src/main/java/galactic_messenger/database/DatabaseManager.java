package galactic_messenger_test.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
	
	public static final byte[] salt = {0, 8, 6, 7, 6, 7};
	private static ConcurrentHashMap<String, String> passwords = new ConcurrentHashMap<String, String>();
	
	public static void loadUsers() {
		BufferedReader reader;

		try {
			File dbFile = new File("database.db");
			
			if (dbFile.exists()) {
				reader = new BufferedReader(new FileReader(dbFile));
				String line = reader.readLine();

				while (line != null) {
					System.out.println(line);
					// read next line
					line = reader.readLine();
					if (line != null)
						appendUser(line);
					
				}

				reader.close();
				exportToDatabase();
			} else {
				dbFile.createNewFile();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void appendUser(String fileline) {
		int separatorIndex = fileline.indexOf(':');
		
		if (separatorIndex == -1 || separatorIndex == fileline.length()-1) {
			System.out.println("Skipped line \""+fileline+"\" : bad declaration.");
		}
		
		String user = fileline.substring(0, separatorIndex);
		String password = fileline.substring(separatorIndex+1, fileline.length());
		passwords.put(user, password);
		exportToDatabase();
	}
	
	public static boolean registerUser(String username, String plain_password) {
		if (!username.toUpperCase().equals("SERVER") || username.length() < 4
				||hasUser(username)) {
			return false;
		}
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
			md.update(salt);

			byte[] hashedPassword = md.digest(plain_password.getBytes(StandardCharsets.UTF_8));
			String hashed_pswrd_str = "";
			for(int i = 0; i < hashedPassword.length; i++) {
				hashed_pswrd_str += (char) hashedPassword[i];
			}
			passwords.put(username, hashed_pswrd_str);
			return true;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static void exportToDatabase() {
		File dbFile = new File("database.db");
		try {
			if (!dbFile.exists()) {
				dbFile.createNewFile();
			}
			
			FileWriter writer = new FileWriter("database.db");

			for(String user : passwords.keySet()) {
				writer.write(user+":"+passwords.get(user)+"\n");
			}
			
			writer.close();
		} catch(IOException e) {
			System.out.println("Could not write to database: "+e.getMessage());
		}
	}
	
	public static boolean hasUser(String username) {
		return passwords.containsKey(username);
	}
	
	public static String getPassword(String username) {
		return passwords.get(username);
	}

}
