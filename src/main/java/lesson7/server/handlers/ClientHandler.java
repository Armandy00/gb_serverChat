package lesson7.server.handlers;


import lesson7.server.MyServer;
import lesson7.server.services.AuthenticationService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private static final String AUTH_CMD_PREFIX = "/auth";
    private static final String AUTHOK_CMD_PREFIX = "/authok";
    private static final String AUTHERR_CMD_PREFIX = "/autherr";
    private static final String CLIENT_MSG_CMD_PREFIX = "/cMsg";
    private static final String SERVER_MSG_CMD_PREFIX = "/sMsg";
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pMsg";
    private static final String STOP_SERVER_CMD_PREFIX = "/stop";
    private static final String END_CLIENT_CMD_PREFIX = "/end";

    private MyServer myServer;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    public String getUsername() {
        return username;
    }

    private void readMessage() throws IOException {
        while (true) {
            String message = in.readUTF();
            System.out.println("message | " + username + ": " + message);
            String[] typeMessage = message.split("\\s+");
            switch (typeMessage[0]) {
                case STOP_SERVER_CMD_PREFIX -> myServer.stop();
                case END_CLIENT_CMD_PREFIX -> closeConnection();
                case PRIVATE_MSG_CMD_PREFIX -> myServer.privateMessage(this, message);
                default -> myServer.broadcastMessage(this, message);

            }
        }
    }

    public void  sendMessage(String sender, String message) throws IOException {
        out.writeUTF(String.format("%s %s %s", CLIENT_MSG_CMD_PREFIX, sender, message));

    }

    private void closeConnection() throws IOException {
        clientSocket.close();
        System.out.println(username + " покинул чат");
    }

    private String username;

    public ClientHandler(MyServer myServer, Socket socket) {
        this.clientSocket = socket;
        this.myServer = myServer;

    }

    public void handle() throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());
        new Thread(() -> {
            try {
                authentication();
                readMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void authentication() throws IOException {

        while (true) {
            String message = in.readUTF(); //ждем и читаем сообщение от клиента
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                boolean isSuccessAuth = processAuthentication(message);
                if (isSuccessAuth) {
                    break;

                }
            } else {

                out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная команда аутентификации");
                System.out.println("Неверная команда аутентификации");
            }

        }
    }

    private boolean processAuthentication(String message) throws IOException {
        String[] messageParts = message.split("\\s+");

        if (messageParts.length != 3) {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная команда аутентификации");
            System.out.println("Неверная команда аутентификации");
            return false;
        }
        String login = messageParts[1];
        String password = messageParts[2];

        AuthenticationService auth = myServer.getAuthenticationService();
        username = auth.getUserNameByLoginAndPassword(login, password);

        if (username != null) {
            if (myServer.isUserNameBusy(username)) {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Логин уже используется");
                return false;

            }
            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
            myServer.subscribe(this);
            System.out.println("Пользователь " + username + "подлючился к чату");
            return true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная комбинация логина и пароля");
            return false;

        }
    }

}
