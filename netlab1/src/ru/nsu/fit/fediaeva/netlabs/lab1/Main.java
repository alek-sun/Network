package ru.nsu.fit.fediaeva.netlabs.lab1;

import java.io.IOException;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {
        try {
            if (args.length == 0){
                System.out.println("Введите IP адрес группы первым параметром командной строки");
                return;
            }
            Peer peer = new Peer(args[0]);
            peer.start();
            peer.close();
        } catch (UnknownHostException e){
            System.out.println("Unknown host");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
