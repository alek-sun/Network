package ru.nsu.fit.fediaeva.netlab3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Receiver implements Runnable {
    private InetSocketAddress address;
    private ConcurrentLinkedQueue<Message> messagesOnSending;
    private final ArrayBlockingQueue<Message> messagesOnResending;
    private final ArrayList<InetSocketAddress> connections;
    private int lostPercent;

    Receiver(ConcurrentLinkedQueue<Message> receivedMsg, ArrayBlockingQueue<Message> messagesOnResending,
             InetSocketAddress adr,
             ArrayList<InetSocketAddress> connections, int lostPercent) {
        address = adr;
        this.messagesOnSending = receivedMsg;
        this.messagesOnResending = messagesOnResending;
        this.connections = connections;
        this.lostPercent = lostPercent;
    }

    private void addMsg(Message message) {
        switch (message.getType()) {
            case "ACK":
                removeAcked(message, messagesOnSending);
                synchronized (messagesOnResending) {
                    removeAcked(message, messagesOnResending);
                }
                break;
            case "REQUEST":
                synchronized (messagesOnResending) {
                    if (containsMsg(message, messagesOnSending) || containsMsg(message, messagesOnResending)) {
                        messagesOnSending.add(generateAck(message));
                        return;
                    }
                }
                System.out.println("REQUEST RECEIVED");
                messagesOnSending.add(message);
                synchronized (connections) {
                    connections.add(message.getSender());
                    System.out.println("Added connection : " + message.getSender());
                }
                messagesOnSending.add(generateAck(message));
                break;
            default:
                synchronized (messagesOnResending) {
                    if (containsMsg(message, messagesOnSending) || containsMsg(message, messagesOnResending)) {
                        messagesOnSending.add(generateAck(message));
                        return;
                    }
                }

                messagesOnSending.add(message);
                System.out.println("RECEIVED : " + message.getData());
                synchronized (connections) {
                    for (InetSocketAddress adr : connections) {
                        if (!adr.equals(message.getSender())) {
                            messagesOnSending.add(new Message(message.getData(), message.getType(), address, adr, UUID.randomUUID()));
                        } else {
                            messagesOnSending.add(generateAck(message));
                        }
                    }
                }
                break;
        }
    }

    private void removeAcked(Message message, AbstractQueue<Message> q) {
        q.removeIf(m -> {
            if (m.getGuid().equals(UUID.fromString(message.getData()))) {
                if (m.getType().equals("REQUEST")) {
                    System.out.println("Connection confirmed by " + m.getReceiver());
                }
                return true;
            }
            return false;
        });
    }

    private Message generateAck(Message message) {
        return new Message(message.getGuid().toString(), "ACK", address, message.getSender(), UUID.randomUUID());
    }

    private boolean containsMsg(Message m, AbstractQueue<Message> q){
        for (Message msg : q) {
            if (msg.getGuid().equals(m.getGuid())){
                msg.setLastReceived(System.currentTimeMillis());
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        try {
            byte[] buf = new byte[1024];
            DatagramSocket socket = new DatagramSocket(address);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            Message m;
            Random rnd = new Random(System.currentTimeMillis());
            while (true) {
                socket.receive(packet);
                if (rnd.nextInt(100) < lostPercent){
                    continue;
                }
                String data = new String(buf, 0, packet.getLength(), StandardCharsets.UTF_8);
                String[] msgParts = data.split(":", 4);
                String type = msgParts[0];
                String guid = msgParts[1];
                int port = Integer.parseInt(msgParts[2]);
                String msgContent = msgParts[3];
                InetSocketAddress sender = new InetSocketAddress(packet.getAddress(), port);
                m = new Message(msgContent, type, sender, address, UUID.fromString(guid));
                addMsg(m);
            }
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
