package ru.nsu.fit.fediaeva.netlab5server;

import javax.websocket.RemoteEndpoint;
import java.util.UUID;

public class User {
    private String name;
    private RemoteEndpoint.Basic chatEndpoint;
    private String token;
    private boolean isOnline;
    private static long globalId = 0;
    private long id;
    private Long lastMsgTime;

    public User(RemoteEndpoint.Basic chatEndpoint, String name, boolean isOnline) {
        globalId++;
        id = globalId;
        this.chatEndpoint = chatEndpoint;
        this.name = name;
        this.token = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.isOnline = isOnline;
        lastMsgTime = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public long getId() {
        return id;
    }

    Long getLastMsgTime() {
        return lastMsgTime;
    }

    public void setLastMsgTime(Long lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }

    public RemoteEndpoint.Basic getChatEndpoint() {
        return chatEndpoint;
    }
}
