package ru.nsu.fit.fediaeva.netlabs.lab1;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Peer {
    private static final int port = 6789;
    private HashMap<String, Long> peers;
    private Long lastSendTime;
    private Long lastRecvTime;
    private MulticastSocket sock;
    private InetAddress group;

    public Peer(String addr) throws IOException {
        peers = new HashMap<>();
        group = InetAddress.getByName(addr);
        sock = new MulticastSocket(port);
        sock.setSoTimeout(1000);
        sock.joinGroup(group);
    }

    private void send(String data) throws IOException {
        byte[] bData = data.getBytes();
        DatagramPacket pack = new DatagramPacket(
                bData,
                bData.length,
                group,
                port
        );
        sock.send(pack);
        lastSendTime = System.currentTimeMillis();
    }

    private String recieve() throws IOException {
        byte[] bData = new byte[1024];
        DatagramPacket recv = new DatagramPacket(bData, bData.length);
        try {
            sock.receive(recv);
            lastRecvTime = System.currentTimeMillis();
        } catch (SocketTimeoutException e) {
            return null;
        }
        return recv.getAddress().toString();
    }

    public void close() throws IOException {
        sock.leaveGroup(group);
    }

    public void start() throws IOException {
        lastSendTime = System.currentTimeMillis();
        while (true) {
            if ((System.currentTimeMillis() - lastSendTime) > 3000) {
                send("Hello");
            }
            String ipPeer = recieve();
            if (ipPeer == null) continue;
            if (peers.containsKey(ipPeer)) {
                peers.replace(ipPeer, lastRecvTime);
            } else {
                peers.put(ipPeer, lastRecvTime);
                printPeerList();
            }
            if (cleanup()){
                printPeerList();
            }
        }
    }

    private void printPeerList() {
        System.out.println("-----------------------------------");
        peers.forEach((s, aLong) -> System.out.println("IP " + s + " : " + toDate(aLong)));
    }

    private boolean cleanup() {
        ArrayList<String> onDelete = new ArrayList<>();
        peers.forEach((s, aLong) -> {
            Long curTime = System.currentTimeMillis();
            if ((curTime - aLong) > 10000) {
                onDelete.add(s);
            }
        });
        if (onDelete.isEmpty()) return false;
        for (String s : onDelete) {
            peers.remove(s);
        }
        return true;
    }

    private String toDate(Long curTime) {
        long millis = curTime % 1000;
        long second = (curTime / 1000) % 60;
        long minute = (curTime / (1000 * 60)) % 60;
        long hour = (curTime / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
    }
}
