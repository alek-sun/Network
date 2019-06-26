package ru.nsu.fit.fediaeva.netlab3;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class Connector implements Runnable {
    private ConcurrentLinkedQueue<Message> msgOnSending;
    private final ArrayBlockingQueue<Message> msgOnResending;
    private Receiver receiver;
    private final ArrayList<InetSocketAddress> connections;
    private InetSocketAddress receiverAddr;
    private InetSocketAddress senderAddr;

    Connector(InetSocketAddress address, int lostPercent) {
        msgOnSending = new ConcurrentLinkedQueue<>();
        connections = new ArrayList<>();
        receiverAddr = address;
        msgOnResending = new ArrayBlockingQueue<>(20);
        receiver = new Receiver(msgOnSending, msgOnResending, receiverAddr, connections, lostPercent);
        senderAddr = new InetSocketAddress(receiverAddr.getHostString(), receiverAddr.getPort()+1);
    }

    void addConnection(InetSocketAddress adr) {
        synchronized (connections) {
            connections.add(adr);
        }
    }

    void addMsg(String data, String type) {
        synchronized (connections){
            for (InetSocketAddress recvAdr : connections) {
                msgOnSending.add(new Message(data, type, receiverAddr, recvAdr, UUID.randomUUID()));
            }
        }
    }

    @Override
    public void run() {
        Thread recvThread = new Thread(receiver);
        recvThread.start();
        try {
            DatagramSocket socket = new DatagramSocket(senderAddr);
            while (true) {
                Iterator<Message> it = msgOnSending.iterator();
                while (it.hasNext()){
                    Message m = it.next();
                    if (m.getType().equals("ACK")){
                        sendMsg(socket, m);
                        it.remove();
                        continue;
                    }
                    if (m.getStartTime().equals(0L)){
                        m.setStartTime(System.currentTimeMillis());
                        sendMsg(socket, m);
                        synchronized (msgOnResending) {
                            msgOnResending.add(m);
                        }
                        it.remove();
                    }
                }
                Message msg;
                ArrayList<Message> v = new ArrayList<>();
                synchronized (msgOnResending) {
                    while ((msg = msgOnResending.poll(3, TimeUnit.SECONDS)) != null) {
                        if (msg.timeToDeleteConnection()) {
                            synchronized (connections) {
                                connections.remove(msg.getReceiver());
                                System.out.println("Connection removed " + msg.getReceiver());
                            }
                        } else {
                            sendMsg(socket, msg);
                            if (!msg.timeToDeleteMessage()) {
                                v.add(msg);
                            }
                        }
                    }
                    msgOnResending.addAll(v);
                }
            }
        } catch (SocketException | InterruptedException e) {
            System.out.println(e.getMessage());
        }

    }

    private void sendMsg(DatagramSocket socket, Message m){
        if (m.getReceiver().equals(receiverAddr)) return;
        try {
            String packData = m.getType() + ":" + m.getGuid() + ":" + receiverAddr.getPort() + ":" + m.getData();
            byte[] buf = packData.getBytes(StandardCharsets.UTF_8);
            DatagramPacket pack = new DatagramPacket(buf, 0, buf.length, m.getReceiver());
            socket.send(pack);
        } catch (UnsupportedEncodingException e){
            System.out.println(e.getMessage());
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
