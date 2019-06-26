package ru.nsu.fit.fediaeva.netlab5server;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/login")
public class LoginEndpoint {

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("open");
        session.getBasicRemote().sendText("onOpen");
    }

    @OnMessage
    public void handle(String message) {
        System.out.println("message");
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("close");
    }
}