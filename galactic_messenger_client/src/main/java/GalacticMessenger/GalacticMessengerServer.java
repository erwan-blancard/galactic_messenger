import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class GalacticMessengerServer {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                    String clientRequest = in.readLine();

                    if (clientRequest != null) {
                        // Gérer les commandes du client (enregistrer, se connecter, etc.)
                        String[] parts = clientRequest.split(" ");
                        String command = parts[0];

                        if (command.equals("/register")) {
                            // Traiter l'enregistrement ici
                            String username = parts[1];
                            String password = parts[2];
                            out.println("Registration successful.");
                        } else if (command.equals("/login")) {
                            // Traiter la connexion ici
                            String username = parts[1];
                            String password = parts[2];
                            out.println("Login successful.");
                        } else if (command.equals("/help")) {
                            out.println("Available commands: /register, /login, /help, /exit");
                        } else if (command.equals("/exit")) {
                            out.println("Goodbye!");
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
