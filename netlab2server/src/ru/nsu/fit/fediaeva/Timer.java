package ru.nsu.fit.fediaeva;

import ru.nsu.fit.fediaeva.threadpool.SocketTask;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Timer implements Runnable{
    private int timeOut;    // period of the updating in seconds
    private final ArrayList<SocketTask> clients;

    public Timer(int timeOut){
        clients = new ArrayList<>();
        this.timeOut = timeOut;
    }

    public void addClient(SocketTask socketTask){
        synchronized (clients) {
            clients.add(socketTask);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(timeOut);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int i;
            synchronized (clients) {
                if (clients.isEmpty()) {
                    System.out.println("No clients yet...");
                    continue;
                }
                System.out.println("============= Clients ==============");
                for (i = 0; i < clients.size(); i++) {
                    printLogs(clients.get(i));
                    if (clients.get(i).isCompleted()) {
                        clients.remove(i);
                        i--;
                    }
                }
            }
        }
    }

    private void printLogs(SocketTask client) {
        System.out.println("IP " + client.getIP());
        System.out.println("Speed : " + (client.getGlobalCountData()-client.getLastCountData())/timeOut/1024 + " Kb/sec");
        client.setLastCountData(client.getGlobalCountData());
        System.out.println("Global speed : " + client.getGlobalCountData()/
                (System.currentTimeMillis()-client.getStartTime())*1000/1024 + " Kb/sec");
        System.out.println("Global count received data : " + client.getGlobalCountData()/1024 + " Kb");
        System.out.println("-----------------------------------");

    }
}

