package ru.nsu.fit.fediaeva.netlabs.netlab5;

import javax.websocket.Session;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Sender implements Runnable {
    private ArrayBlockingQueue<String> messagesOnSending;
    private Session session;
    private CountDownLatch latch;

    Sender(ArrayBlockingQueue<String> messagesOnSending, Session session, CountDownLatch latch) {
        this.messagesOnSending = messagesOnSending;
        this.session = session;
        this.latch = latch;
    }

    @Override
    public void run() {
        while (latch.getCount() > 0) {
            try {
                String msgType;
                String msg = messagesOnSending.take();
                switch (msg) {
                    case ("-users") : {
                        msgType = "userlist";
                        break;
                    }
                    case ("-messages") : {
                        msgType = "msglist";
                        break;
                    }
                    case ("-exit") : {
                        msgType = "logout";
                        break;
                    }
                    default: msgType = "usual";
                }
                session.getAsyncRemote().sendText(msgType + "%%" + msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
