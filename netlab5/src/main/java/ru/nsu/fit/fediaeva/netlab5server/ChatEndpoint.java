package ru.nsu.fit.fediaeva.netlab5server;

import ru.nsu.fit.fediaeva.netlab5server.matchers.IntMatcher;
import ru.nsu.fit.fediaeva.netlab5server.matchers.Matcher;
import ru.nsu.fit.fediaeva.netlab5server.matchers.StringMatcher;
import ru.nsu.fit.fediaeva.netlab5server.requesthandlers.*;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ServerEndpoint(value = "/login")
public class ChatEndpoint {
    private final ArrayList<User> peers = new ArrayList<>();
    private ArrayList<Message> messages;
    private ArrayList<User> users;
    private HandlersTree handlersTree;
    private ThreadPoolExecutor threadPool;

    ChatEndpoint (){
        handlersTree = new HandlersTree();
        messages = new ArrayList<>();
        users = new ArrayList<>();
        fillHandlersTree();
    }

    void start(){
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(30);
        threadPool = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, queue);
        Timer timer = new Timer(users, messages);
        threadPool.execute(timer);
    }

    @OnOpen
    public void onOpen(Session session, String message) throws IOException {
        System.out.println("open");
        session.getBasicRemote().sendText("ack");
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText("received " + message);
        /*synchronized (peers) {
            peers.add(new User(session.getBasicRemote(), ));
        }*/
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        System.out.println("close");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private void broadcast(String message) {
        synchronized (peers) {
            peers.forEach(p -> {
                    try {
                        p.getChatEndpoint().sendText(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
    }

    private void fillHandlersTree() {
        ArrayList<Matcher> postMsg = new ArrayList<>() {{
            add(new StringMatcher("POST"));
            add(new StringMatcher("messages"));
        }};
        handlersTree.addHandler(postMsg.iterator(), new AddMessage(peers, messages));

        ArrayList<Matcher> getMessages = new ArrayList<>() {{
            add(new StringMatcher("GET"));
            add(new StringMatcher("messages"));
        }};
        handlersTree.addHandler(getMessages.iterator(), new MessagesGetter(messages));

        ArrayList<Matcher> getCountMessages = new ArrayList<>() {{
            add(new StringMatcher("GET"));
            add(new StringMatcher("messages"));
            add(new StringMatcher("count="));
        }};
        handlersTree.addHandler(getCountMessages.iterator(), new CountMessageGetter(messages));

        ArrayList<Matcher> getCountOffMessages = new ArrayList<>() {{
            add(new StringMatcher("GET"));
            add(new StringMatcher("messages"));
            add(new StringMatcher("offset="));
            add(new StringMatcher("count="));
        }};
        handlersTree.addHandler(getCountOffMessages.iterator(), new OffCountMessageGetter(peers, messages));

        ArrayList<Matcher> getOffMessages = new ArrayList<>() {{
            add(new StringMatcher("GET"));
            add(new StringMatcher("messages"));
            add(new StringMatcher("offset="));
        }};
        handlersTree.addHandler(getOffMessages.iterator(), new OffsetMessagesGetter(messages));

        ArrayList<Matcher> getUsers = new ArrayList<>() {{
            add(new StringMatcher("GET"));
            add(new StringMatcher("users"));
        }};
        handlersTree.addHandler(getUsers.iterator(), new UsersGetter(peers));

        ArrayList<Matcher> getIdUser = new ArrayList<>() {{
            add(new StringMatcher("GET"));
            add(new StringMatcher("users"));
            add(new IntMatcher());
        }};
        handlersTree.addHandler(getIdUser.iterator(), new IdUserGetter(peers));

        ArrayList<Matcher> login = new ArrayList<>() {{
            add(new StringMatcher("POST"));
            add(new StringMatcher("login"));
        }};
        handlersTree.addHandler(login.iterator(), new LoginHandler(peers, messages));

        ArrayList<Matcher> logout = new ArrayList<>() {{
            add(new StringMatcher("POST"));
            add(new StringMatcher("logout"));
        }};
        handlersTree.addHandler(logout.iterator(), new LogoutHandler(peers, messages));
    }
}
