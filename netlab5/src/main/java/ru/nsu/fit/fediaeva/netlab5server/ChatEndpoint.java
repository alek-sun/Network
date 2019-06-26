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




/*
import ru.nsu.fit.fediaeva.netlab5server.matchers.IntMatcher;
import ru.nsu.fit.fediaeva.netlab5server.matchers.Matcher;
import ru.nsu.fit.fediaeva.netlab5server.matchers.StringMatcher;
import ru.nsu.fit.fediaeva.netlab5server.requesthandlers.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class Server {
    private ArrayList<User> users;
    private ArrayList<Message> messages;
    private ServerSocket socket;
    private HandlersTree handlersTree;

    Server(String host, int port) throws IOException {
        messages = new ArrayList<>();
        users = new ArrayList<>();
        socket = new ServerSocket();
        socket.bind(new InetSocketAddress(host, port));
        handlersTree = new HandlersTree();
        fillHandlersTree();
    }

    private void fillHandlersTree() {
        ArrayList<Matcher> postMsg = new ArrayList<>() {{
            add(new StringMatcher("POST"));
            add(new StringMatcher("messages"));
        }};
        handlersTree.addHandler(postMsg.iterator(), new AddMessage(users, messages));

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
        handlersTree.addHandler(getCountOffMessages.iterator(), new OffCountMessageGetter(users, messages));

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
        handlersTree.addHandler(getUsers.iterator(), new UsersGetter(users));

        ArrayList<Matcher> getIdUser = new ArrayList<>() {{
            add(new StringMatcher("GET"));
            add(new StringMatcher("users"));
            add(new IntMatcher());
        }};
        handlersTree.addHandler(getIdUser.iterator(), new IdUserGetter(users));

        ArrayList<Matcher> login = new ArrayList<>() {{
            add(new StringMatcher("POST"));
            add(new StringMatcher("login"));
        }};
        handlersTree.addHandler(login.iterator(), new LoginHandler(users, messages));

        ArrayList<Matcher> logout = new ArrayList<>() {{
            add(new StringMatcher("POST"));
            add(new StringMatcher("logout"));
        }};
        handlersTree.addHandler(logout.iterator(), new LogoutHandler(users, messages));
    }

    void start() {
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(30);
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, queue);
        Timer timer = new Timer(users, messages);
        threadPool.execute(timer);
        while (true) {
            try {
                Socket s = socket.accept();
                Message message = new Message();
                RequestHandler handler;
                BufferedInputStream in;
                try {
                    in = new BufferedInputStream(s.getInputStream());
                    handler = findHandler(in, message);
                } catch (ServerException e) {
                    System.out.println("SERVER ERR");
                    ErrorResponseSender sender = new ErrorResponseSender(
                            e.getTitle(),
                            e.getInfo(),
                            new BufferedWriter(new OutputStreamWriter(s.getOutputStream())));
                    threadPool.execute(sender);
                    continue;
                }
                ResponseSender sender = new ResponseSender(in, s.getOutputStream(),
                        users, handler, message);
                threadPool.execute(sender);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static String readBufferedLine(BufferedInputStream in) throws ServerException {
        ArrayList<Byte> bytes = new ArrayList<>();
        int c;
        while (true) {
            try {
                c = in.read();
            } catch (IOException e) {
                throw new ServerException(500, "Can't read request");
            }
            if (c < 0) {
                return "";
            }
            byte b = (byte) c;
            try {
                bytes.add(b);
            } catch (OutOfMemoryError e) {
                System.out.println(bytes.size());
            }
            if (b == '\n') {
                StringBuilder res = new StringBuilder();
                bytes.remove(bytes.size() - 1);
                if (bytes.get(bytes.size() - 1) == '\r') {
                    bytes.remove(bytes.size() - 1);
                }
                for (Byte bt : bytes) {
                    res.append((char)(byte)bt);
                }
                return res.toString();
            }
        }
    }

    private RequestHandler findHandler(BufferedInputStream in, Message m) throws ServerException {
        String firstLine;
        firstLine = readBufferedLine(in);
        System.out.println(firstLine);
        String[] methodPathVersion = firstLine.split(" ", 3);
        ArrayList<String> pathForTree = new ArrayList<>();

        String method = methodPathVersion[0];
        m.setMethod(method);
        pathForTree.add(method);
        String path = methodPathVersion[1];
        path = path.replaceFirst("/", "");
        m.setPath(path);

        String[] pathParts = path.split("[?&/]");

        // "count=<count>" => "count="
        for (int i = 0; i < pathParts.length; i++) {
            String[] elTokens = pathParts[i].split("=");
            if (elTokens.length == 2) {
                pathParts[i] = elTokens[0] + "=";
            }
        }
        ArrayList<String> partsList = new ArrayList<>(Arrays.asList(pathParts));
        pathForTree.addAll(partsList);
        RequestHandler handler = handlersTree.getHandler(pathForTree.iterator());
        if (handler == null) {
            throw new ServerException(405, "No such method");
        }
        return handler;
    }
}
*/
