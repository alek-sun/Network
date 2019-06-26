package netlab5.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;

@ServerEndpoint("/login")
public class Server {
    private static final ArrayList<User> clients = new ArrayList<>();
    private static ArrayList<Message> messages = new ArrayList<>();

    static void broadcast(String message) {
        clients.forEach(user -> user.getSession().getAsyncRemote().sendText(message));
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("new session!");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        System.out.println(message);
        String[] msgParts = message.split("%%", 3);
        if (!checkLen(session, msgParts, 2)) return;
        if (msgParts[0].equals("login")) {
            User u = new User(session, msgParts[1]);
            broadcast("usual%%New user " + u.getName());
            clients.add(u);
            session.getAsyncRemote().sendText("login%%" + session.getId());
            sendMessageList(session);
            return;
        }
        User sender;
        if ((sender = findUser(session)) == null) {
            session.getAsyncRemote().sendText("usual%%Error >> Unknown token");
            return;
        }
        handleMessage(msgParts[0], msgParts[1], session, sender);
    }

    private boolean checkLen(Session session, String[] msgParts, int i) {
        if (msgParts.length < i) {
            session.getAsyncRemote().sendText("usual%%Error >> Bad request");
            return false;
        }
        return true;
    }

    private User findUser(Session session) {
        for (User u : clients) {
            if (u.getSession().getId().equals(session.getId())) {
                return u;
            }
        }
        return null;
    }

    private void handleMessage(String type, String body, Session session, User sender) {
        switch (type) {
            case ("usual"): {
                messages.add(new Message(sender, body));
                broadcast("usual%%" + sender.getName() + " : " + body);
                return;
            }
            case ("logout"): {
                clients.remove(sender);
                broadcast("usual%%Exit user " + sender.getName());
                session.getAsyncRemote().sendText("logout%%Successful exit");
                return;
            }
            case ("msglist"): {
                sendMessageList(session);
                return;
            }
            case ("userlist"): {
                sendUserList(session);
            }
        }
    }

    private void sendUserList(Session session) {
        JSONObject userList = new JSONObject();
        JSONArray userArr = new JSONArray();
        clients.forEach(c -> {
            JSONObject client = new JSONObject();
            client.put("name", c.getName());
            client.put("id", c.getId());
            userArr.add(client);
        });
        userList.put("users", userArr);
        session.getAsyncRemote().sendText("userlist%%" + userList.toJSONString());
    }

    private void sendMessageList(Session session) {
        JSONObject messageList = new JSONObject();
        JSONArray msgArr = new JSONArray();
        messages.forEach(m -> {
            JSONObject message = new JSONObject();
            message.put("sender", m.getSender().getName());
            message.put("data", m.getData());
            msgArr.add(message);
        });
        messageList.put("messages", msgArr);
        session.getAsyncRemote().sendText("msglist%%" + messageList.toJSONString());
    }

    @OnClose
    public void onClose(Session session) {
        User sender = findUser(session);
        if (sender == null) {
            session.getAsyncRemote().sendText("usual%%Error >> token error");
            return;
        }
        clients.remove(sender);
        broadcast("usual%%Exit user " + sender.getName());
        session.getAsyncRemote().sendText("logout%%Successful exit");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("err!");
    }

}