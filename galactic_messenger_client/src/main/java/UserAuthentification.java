import org.mindrot.jbcrypt.BCrypt;

public class UserAuthentication {
    public static boolean authenticateUser(String inputPassword, String hashedPassword) {
        return BCrypt.checkpw(inputPassword, hashedPassword);
    }

    public static String hashPassword(String plainPassword) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(plainPassword, salt);
    }
}
