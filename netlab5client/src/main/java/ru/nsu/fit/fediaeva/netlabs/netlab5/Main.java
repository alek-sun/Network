package ru.nsu.fit.fediaeva.netlabs.netlab5;

public class Main {
    public static void main(String[] args) {
        WebSocketClient client = new WebSocketClient(args[0]);
        client.start();
    }
}
