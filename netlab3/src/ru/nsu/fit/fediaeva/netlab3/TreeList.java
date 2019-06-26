package ru.nsu.fit.fediaeva.netlab3;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

class TreeList {
    private InetSocketAddress adress;
    private Connector connector;

    TreeList(String ip, int port, String parentIP, int parentPort, int lostPercent) {
        adress = new InetSocketAddress(ip, port);
        InetSocketAddress parentAdr = new InetSocketAddress(parentIP, parentPort);
        connector = new Connector(adress, lostPercent);
        connector.addConnection(parentAdr);
        connector.addMsg("I'M HERE", "REQUEST");
        Thread connectorThread = new Thread(connector);
        connectorThread.start();
    }

    TreeList(String ip, int port, int lostPercent) {
        adress = new InetSocketAddress(ip, port);
        connector = new Connector(adress, lostPercent);
        Thread connectorThread = new Thread(connector);
        connectorThread.start();
    }

    void start() {
        Scanner sc = new Scanner(System.in);
        String msg;
        while (true) {
            msg = sc.nextLine();
            if (msg.getBytes(StandardCharsets.UTF_8).length > 800){
                System.out.println("Please, enter not so big message");
                continue;
            }
            connector.addMsg(msg, "USUAL");
        }
    }


}
