package lesson7.server.models;

import lombok.Data;

@Data
public class User {
    private final String LOGIN;
    private final String PASSWORD;
    private final String USERNAME;

}
