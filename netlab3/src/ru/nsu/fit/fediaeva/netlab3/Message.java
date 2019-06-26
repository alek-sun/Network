package ru.nsu.fit.fediaeva.netlab3;

import java.net.InetSocketAddress;
import java.util.UUID;

public class Message {
    private String data;
    private Long startTime;
    private Long lastReceived;
    private String type;
    private InetSocketAddress sender;
    private InetSocketAddress receiver;
    private UUID guid;

    Message(String data, String type, InetSocketAddress sender, InetSocketAddress receiver, UUID guid) {
        this.data = data;
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.guid = guid;
        startTime = 0L;
        lastReceived = 0L;
    }

    boolean timeToDeleteConnection() {
        return System.currentTimeMillis() - startTime > 60_000;
    }

    boolean timeToDeleteMessage(){
        return  lastReceived != 0L && (System.currentTimeMillis() - lastReceived) > 30_000;
    }

    void setLastReceived(Long lastReceived) {
        this.lastReceived = lastReceived;
    }

    InetSocketAddress getReceiver() {
        return receiver;
    }

    UUID getGuid() {
        return guid;
    }

    void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    String getType() {
        return type;
    }

    Long getStartTime() {
        return startTime;
    }

    String getData() {
        return data;
    }

    InetSocketAddress getSender() {
        return sender;
    }
}
