package ru.nsu.fit.fediaeva.netlab5server.requesthandlers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.nsu.fit.fediaeva.netlab5server.*;

import java.util.ArrayList;

public class OffCountMessageGetter implements RequestHandler {
    private final ArrayList<User> users;
    private final ArrayList<Message> messages;

    public OffCountMessageGetter(ArrayList<User> users, ArrayList<Message> messages) {
        this.users = users;
        this.messages = messages;
    }

    @Override
    public Message getResponse(Message request) throws ServerException {
        //path : messages?offset=<offset>&count=<count>
        String[] pathArr = request.getPath().split("[?&=]");
        if (pathArr.length < 5) {
            throw new ServerException(405, "");
        }
        Integer offset = Integer.valueOf(pathArr[2]);
        int count = Integer.parseInt(pathArr[4]);
        if (count > 100 || count < 0) {
            throw new ServerException(400, "Bad count of messages. Enter count [0,100])");
        }
        JSONObject respBody = new JSONObject();
        JSONArray msgArr = new JSONArray();

        // count = 1 for long polling
        if (count == 1) {
            while (true) {
                //updateUserTime(request.getToken());
                int size;
                synchronized (messages) {
                    size = messages.size();
                }
                if (offset < size) {
                    ArrayList<Message> messageArrayList;
                    synchronized (messages) {
                        messageArrayList = new ArrayList<>(messages);
                    }
                    for (int i = offset; i < messageArrayList.size(); i++) {
                        MessagesGetter.createMsgObj(msgArr, messageArrayList.get(i));
                    }
                    break;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new ServerException(500, "Server interrupted");
                }
            }
        } else {
            synchronized (messages) {
                if (offset >= messages.size()) {
                    return createHTTPMessage(respBody, msgArr);
                }
            }
            ArrayList<Message> messageArrayList;
            synchronized (messages) {
                messageArrayList = new ArrayList<>(messages);
            }
            for (int i = offset; i < messageArrayList.size() && i < offset + count; i++) {
                MessagesGetter.createMsgObj(msgArr, messageArrayList.get(i));
            }
            messageArrayList.clear();
        }
        return createHTTPMessage(respBody, msgArr);
    }

    private Message createHTTPMessage(JSONObject respBody, JSONArray msgArr) {
        respBody.put("messages", msgArr);
        Message response = new Message();
        response.setOKTitle();
        response.getHeaders().put("Content-Type", "application/json");
        response.setBody(respBody.toJSONString());
        return response;
    }

    private void updateUserTime(String token) {
        synchronized (users) {
            for (User u : users) {
                if (u.getToken().equals(token)){
                    u.setLastMsgTime(System.currentTimeMillis());
                }
            }

        }
    }
}
