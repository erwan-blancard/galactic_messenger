import java.io.*;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) {
        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in)) {

                 String userInputString;
                 String username = "your_username"; // Remplacez par le nom d'utilisateur réel

            while (true) {
                     System.out.println("Options:");
                     System.out.println("1. Initiate Private Chat");
                     System.out.println("2. Respond to Private Chat Request");
                     System.out.println("3. Exit Private Chat");
                     System.out.println("4. Send Message (Inside Private Chat)");
                     System.out.println("5. Exit");
                     System.out.print("Enter your choice: ");
                     userInputString = userInput.readLine();

                     if (userInputString.equals("1")) {
                         // Initier un chat privé
                         System.out.print("Enter the username of the user you want to chat with: ");
                         String targetUsername = userInput.readLine();
                         out.println("/private_chat " + targetUsername);
                     } else if (userInputString.equals("2")) {
                         // Répondre à une demande de chat privé
                         System.out.print("Enter /accept or /decline and the username of the user who initiated the chat: ");
                         String response = userInput.readLine();
                         out.println(response);
                     } else if (userInputString.equals("3")) {
                         // Quitter le chat privé
                         out.println("/exit_private_chat");
                     } else if (userInputString.equals("4")) {
                         // Envoyer un message dans le chat privé
                         System.out.print("Enter your message: ");
                         String
