package lesson7.server.services;

public interface AuthenticationService {
    String getUserNameByLoginAndPassword(String login, String password);

}
