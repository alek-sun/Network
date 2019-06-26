package ru.nsu.fit.fediaeva.netlab5server;

import java.util.ArrayList;

public class Timer implements Runnable {
    private final ArrayList<User> users;
    private final ArrayList<Message> messages;

    Timer(ArrayList<User> users, ArrayList<Message> messages) {
        this.users = users;
        this.messages = messages;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (users) {
                System.out.println("USERS :: ");
                users.forEach(u -> System.out.println(u.getId() + " " + (System.currentTimeMillis() - u.getLastMsgTime())));
                for (User u : users) {
                    if ((System.currentTimeMillis() - u.getLastMsgTime()) > 600_000 && u.isOnline()) {
                        synchronized (messages) {
                            messages.add(new Message("User " + u.getName() + " timed out", u.getId()));
                        }
                        u.setOnline(false);
                    }
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
