import java.io.*;
import java.net.*;
import java.util.*;

public class FileServer {
    private Map<String, List<String>> conversationFiles = new HashMap<>(); // Conversation/group name to files mapping

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("File server is running on port " + port);

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String clientRequest = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                if (clientRequest.startsWith("/upload")) {
                    // Traitez la demande d'envoi de fichier ici
                    String[] parts = clientRequest.split(" ");
                    String conversationOrGroupName = parts[1];
                    String fileName = parts[2];

                    // Enregistrez le fichier dans la conversation ou le groupe
                    storeFile(conversationOrGroupName, fileName);

                    // Répondre au client
                    String response = "File uploaded successfully.";
                    DatagramPacket responsePacket = new DatagramPacket(response.getBytes(), response.length(), clientAddress, clientPort);
                    socket.send(responsePacket);
                } else if (clientRequest.startsWith("/list_files")) {
                    // Traitez la demande de liste de fichiers ici
                    String[] parts = clientRequest.split(" ");
                    String conversationOrGroupName = parts[1];

                    // Récupérez la liste des fichiers associés à la conversation ou au groupe
                    List<String> files = conversationFiles.get(conversationOrGroupName);

                    // Répondre au client avec la liste des fichiers
                    String response = String.join(", ", files);
                    DatagramPacket responsePacket = new DatagramPacket(response.getBytes(), response.length(), clientAddress, clientPort);
                    socket.send(responsePacket);
                } else if (clientRequest.startsWith("/download")) {
                    // Traitez la demande de téléchargement de fichier ici
                    String[] parts = clientRequest.split(" ");
                    String conversationOrGroupName = parts[1];
                    String fileName = parts[2];

                    // Vérifiez si le fichier existe
                    if (isFileAvailable(conversationOrGroupName, fileName)) {
                        // Répondre au client avec le nom du fichier à télécharger
                        String response = "File ready for download: " + fileName;
                        DatagramPacket responsePacket = new DatagramPacket(response.getBytes(), response.length(), clientAddress, clientPort);
                        socket.send(responsePacket);
                    } else {
                        // Répondre au client que le fichier n'est pas disponible
                        String response = "File not available.";
                        DatagramPacket responsePacket = new DatagramPacket(response.getBytes(), response.length(), clientAddress, clientPort);
                        socket.send(responsePacket);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeFile(String conversationOrGroupName, String fileName) {
        // Implémentez la logique pour stocker le fichier associé à la conversation ou au groupe.
        // Utilisez la carte conversationFiles pour cela.
    }

    private boolean isFileAvailable(String conversationOrGroupName, String fileName) {
        // Implémentez la logique pour vérifier si le fichier est disponible pour téléchargement.
        // Consultez la carte conversationFiles.
        return false;
    }
}
