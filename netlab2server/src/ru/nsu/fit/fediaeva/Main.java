package ru.nsu.fit.fediaeva;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            Server server = new Server(8080);
            server.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
