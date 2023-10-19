import java.io.*;
import java.net.Socket;

public class GalacticMessengerClient {
    public static void main(String[] args) {
        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in)) {

                 String userInputString;
            while (true) {
                     // Afficher les options pour l'utilisateur
                     System.out.println("Options:");
                     System.out.println("1. Register");
                     System.out.println("2. Login");
                     System.out.println("3. Help");
                     System.out.println("4. Exit");
                     System.out.print("Enter your choice: ");
                     userInputString = userInput.readLine();

                     if (userInputString.equals("1")) {
                         // S'enregistrer
                         System.out.print("Enter your username: ");
                         String username = userInput.readLine();
                         System.out.print("Enter your password: ");
                         String password = userInput.readLine();
                         out.println("/register " + username + " " + password);
                     } else if (userInputString.equals("2")) {
                         // Se connecter
                         System.out.print("Enter your username: ");
                         String username = userInput.readLine();
                         System.out.print("Enter your password: ");
                         String password = userInput.readLine();
                         out.println("/login " + username + " " + password);
                     } else if (userInputString.equals("3")) {
                         // Afficher l'aide
                         System.out.println("Available commands: /register, /login, /help, /exit");
                     } else if (userInputString.equals("4")) {
                         // Quitter l'application
                         System.out.println("Exiting...");
                         out.println("/exit");
                         break;
                     } else {
                         System.out.println("Invalid choice. Please select a valid option.");
                     }

                     // Lire et afficher la réponse du serveur
                     String serverResponse = in.readLine();
                     System.out.println("Server response: " + serverResponse);
                 }
             } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
