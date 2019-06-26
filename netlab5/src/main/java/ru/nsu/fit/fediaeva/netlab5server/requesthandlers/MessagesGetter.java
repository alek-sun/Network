package ru.nsu.fit.fediaeva.netlab5server.requesthandlers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.ServerException;

import java.util.ArrayList;

public class MessagesGetter implements RequestHandler {
    private final ArrayList<Message> messages;

    public MessagesGetter(ArrayList<Message> messages) {
        this.messages = messages;
    }

    @Override
    public Message getResponse(Message request) throws ServerException {
        JSONObject respBody = new JSONObject();
        JSONArray msgArr = new JSONArray();
        synchronized (messages) {
            messages.forEach(message -> createMsgObj(msgArr, message));
        }
        respBody.put("messages", msgArr);

        Message response = new Message();
        response.setOKTitle();
        response.getHeaders().put("Content-Type", "application/json");
        response.setBody(respBody.toJSONString());
        return response;
    }

    static void createMsgObj(JSONArray msgArr, Message msg) {
        /*JSONObject msgObj = new JSONObject();
        msgObj.put("id", msg.getId());
        msgObj.put("message", msg.getData());
        msgObj.put(("author"), msg.getSenderId());
        msgArr.add(msgObj);*/
    }
}
