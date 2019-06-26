package ru.nsu.fit.fediaeva.netlab5server;

import javax.websocket.RemoteEndpoint;

public class Message {
    private String data;
    private RemoteEndpoint.Basic sender;

    public Message(String data, RemoteEndpoint.Basic sender) {
        this.data = data;
        this.sender = sender;
    }

    public String getData() {
        return data;
    }

    public RemoteEndpoint.Basic getSender() {
        return sender;
    }
}
