package galactic_messenger_test.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import galactic_messenger_test.database.DatabaseManager;

public class UserAuthentication {
	
	public static boolean authenticate(String username, String plain_password) {
		if (DatabaseManager.hasUser(username)) {
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-512");
				md.update(DatabaseManager.salt);

				byte[] hashedPassword = md.digest(plain_password.getBytes(StandardCharsets.UTF_8));
				String hashed_pswrd_str = "";
				for(int i = 0; i < hashedPassword.length; i++) {
					hashed_pswrd_str += (char) hashedPassword[i];
				}
				
				String cmp_passwrd = DatabaseManager.getPassword(username);
				
				if (cmp_passwrd != null)
					return cmp_passwrd.equals(hashed_pswrd_str);
				return false;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
