import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String clientRequest;

            while ((clientRequest = in.readLine()) != null) {
                // Traitez les demandes du client ici
                if (clientRequest.startsWith("/chat_request")) {
                    String targetUsername = clientRequest.split(" ")[1];

                    // Vérifiez si le client B est en ligne et disponible
                    if (isUserOnlineAndAvailable(targetUsername)) {
                        // Acceptez la demande de chat
                        out.println("/chat_accept " + targetUsername);
                    } else {
                        // Rejetez la demande de chat
                        out.println("/chat_reject " + targetUsername);
                    }
                } else {
                    // Traitez d'autres types de demandes ici
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isUserOnlineAndAvailable(String targetUsername) {
        return false; // Exemple simplifié
    }
}
