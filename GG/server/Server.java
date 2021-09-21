package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class Server {

    static final int PORT = 3443;
    private ArrayList<ClientHandler> clients = new ArrayList<>();

    public Server() {
        Socket clientSocket = null;
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started!");
            while (true) {
                clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Server stopped");
                serverSocket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void sendMessageToAllClients(String message) {
        for (ClientHandler o : clients) {
            o.sendMsg(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}
