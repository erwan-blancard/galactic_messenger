import java.io.*;
import java.net.*;

public class FileClient {
    public static void main(String[] args) {
        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            InetAddress serverHost = InetAddress.getByName(serverAddress);

            // Demande d'envoi de fichier
            String uploadRequest = "/upload conversation1 file.txt";
            DatagramPacket uploadPacket = new DatagramPacket(uploadRequest.getBytes(), uploadRequest.length(), serverHost, serverPort);
            socket.send(uploadPacket);

            // Attendre la réponse du serveur
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Server response: " + response);

            // Demande de liste de fichiers
            String listRequest = "/list_files conversation1";
            DatagramPacket listPacket = new DatagramPacket(listRequest.getBytes(), listRequest.length(), serverHost, serverPort);
            socket.send(listPacket);

            // Attendre la réponse du serveur
            socket.receive(receivePacket);
            response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Server response: " + response);

            // Demande de téléchargement de fichier
            String downloadRequest = "/download conversation1 file.txt";
            DatagramPacket downloadPacket = new DatagramPacket(downloadRequest.getBytes(), downloadRequest.length(), serverHost, serverPort);
            socket.send(downloadPacket);

            // Attendre la réponse du serveur
            socket.receive(receivePacket);
            response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Server response: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
