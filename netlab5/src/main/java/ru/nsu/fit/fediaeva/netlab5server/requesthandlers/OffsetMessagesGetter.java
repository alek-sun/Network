package ru.nsu.fit.fediaeva.netlab5server.requesthandlers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.ServerException;

import java.util.ArrayList;

public class OffsetMessagesGetter implements RequestHandler {
    private final ArrayList<Message> messages;

    public OffsetMessagesGetter(ArrayList<Message> messages) {
        this.messages = messages;
    }

    @Override
    public Message getResponse(Message request) throws ServerException {
        String[] pathArr = request.getPath().split("[?=]");
        if (pathArr.length < 3){
            throw new ServerException(405, "");
        }
        int offset = Integer.valueOf(pathArr[2]);
        JSONObject respBody = new JSONObject();
        JSONArray msgArr = new JSONArray();
        ArrayList<Message> messageArrayList;
        synchronized (messages){
            messageArrayList = new ArrayList<>(messages);
        }
        for (int i = offset; i < offset+10 && i < messageArrayList.size(); i++) {
            MessagesGetter.createMsgObj(msgArr, messageArrayList.get(i));
        }
        messageArrayList.clear();
        respBody.put("messages", msgArr);
        Message response = new Message();
        response.setOKTitle();
        response.getHeaders().put("Content-Type", "application/json");
        response.setBody(respBody.toJSONString());
        return response;
    }
}
