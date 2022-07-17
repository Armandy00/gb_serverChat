package lesson7.server;

import lesson7.server.handlers.ClientHandler;
import lesson7.server.models.User;
import lesson7.server.services.AuthenticationService;
import lesson7.server.services.impl.SimpleAuthenticationServiceImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MyServer {


    private final ServerSocket serverSocket;

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    private final AuthenticationService authenticationService;
    private final ArrayList<ClientHandler> clients;

    public MyServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        authenticationService = new SimpleAuthenticationServiceImpl();
        clients = new ArrayList<>();


    }


    public void start() {
        System.out.println("Сервер запущен!");
        System.out.println("-------------------");

        try {
            while (true) {
                waitAndProcessNewClientConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void waitAndProcessNewClientConnection() throws IOException {
        System.out.println("Ожидание клиента.........");
        Socket socket = serverSocket.accept();
        System.out.println("Клиент подключился");

        processClientConnection(socket);
    }

    private void processClientConnection(Socket socket) {
        try {
            ClientHandler handler = new ClientHandler(this, socket);
            handler.handle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler handler) {
        clients.add(handler);

    }

    public synchronized void unSubscribe(ClientHandler handler) {
        clients.remove(handler);

    }

    public boolean isUserNameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void stop() {
        System.out.println("---------------------");
        System.out.println("ЗАВЕРШЕНИЕ РАБОТЫ");
        System.exit(0);
    }

    public void broadcastMessage(ClientHandler sender, String message) throws IOException {

        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendMessage(sender.getUsername(), message);
        }

    }

    public void privateMessage(ClientHandler sender, String message) throws IOException {
        String[] partsMessage = message.split("\\s+");
        String msgNew="";
        for(int i=2;i<partsMessage.length;i++){
            msgNew+=partsMessage[i]+" ";
        }
        ClientHandler client = getClientHandlerByName(partsMessage[1]);
        if (client !=null) {
            client.sendMessage(sender.getUsername(), msgNew); }
    }


    public   ClientHandler getClientHandlerByName(String name) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(name)) {
                return client;
            }

        }
        return null;
    }
}
