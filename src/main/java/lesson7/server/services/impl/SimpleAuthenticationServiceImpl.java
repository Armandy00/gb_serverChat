package lesson7.server.services.impl;

import lesson7.server.handlers.ClientHandler;
import lesson7.server.models.User;
import lesson7.server.services.AuthenticationService;

import java.util.List;

public class SimpleAuthenticationServiceImpl implements AuthenticationService {
    private static final List<User> clients = List.of(
            new User("martin", "1111", "Martin_SuperStar"),
            new User("batman", "1111", "Mouse"),
            new User("yulya", "1111", "yulya-mi-mi")
    );

    @Override
    public String getUserNameByLoginAndPassword(String login, String password) {
        for (User client : clients) {
            if (client.getLOGIN().equals(login) && client.getPASSWORD().equals(password) ) {
                return client.getUSERNAME();
            }
        }
        return null;
    }


}
