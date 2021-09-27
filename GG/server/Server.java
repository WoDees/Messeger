package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

class Server {

    private static final int PORT = 3443;
    private final List<ClientHandler> clients = new ArrayList<>();

    public Server() {
        // В Java есть специальная конструкция, которая называется try-with-resources. Её смысл в том,
        // что она автоматически вызывает метод `close()` в конце блока кода. Воспользовавшись ей, можно сделать
        // код немного короче.
        //
        // 🎓 Reference: https://www.baeldung.com/java-try-with-resources
        // 🎓 Discussion: https://stackoverflow.com/questions/27231193/try-with-resources-closes-sockets-of-spawned-childs
        try (var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started!");
            while (true) {
                var clientSocket = serverSocket.accept();

                var clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);

                var thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server stopped");
        }
    }

    public void sendMessageToAllClients(String message) {
        for (ClientHandler o : clients) {
            o.sendMessage(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}
