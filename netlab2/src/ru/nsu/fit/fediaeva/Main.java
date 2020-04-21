package ru.nsu.fit.fediaeva;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Client client = new Client("M:\\видео\\красиво\\Свет мой тихий..mp4");
        
        try {
            client.connect("localhost", 8080);
        } catch (IOException e) {
            System.out.println("Socket haven't been closed");
        }
    }
}
