package ru.nsu.fit.fediaeva.netlab5server.requesthandlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.ServerException;
import ru.nsu.fit.fediaeva.netlab5server.User;

import java.util.ArrayList;

public class AddMessage implements RequestHandler {
    private final ArrayList<User> users;
    private final ArrayList<Message> messages;

    public AddMessage(ArrayList<User> users, ArrayList<Message> messages) {
        this.users = users;
        this.messages = messages;
    }

    @Override
    public Message getResponse(Message request) throws ServerException {
        JSONParser jsonParser = new JSONParser();
        try {
            Message response = new Message();
            JSONObject reqBody = (JSONObject) jsonParser.parse(request.getBody());
            String message = (String) reqBody.get("message");
            if (message == null) {
                throw new ServerException(400, "Message absent");
            }
            String msgStr = message;
            long senderId = findSender(request.getToken());
            Message msg = new Message(msgStr, senderId);
            synchronized (messages) {
                messages.add(msg);
            }

            //JSONObject body = new JSONObject();
            //body.put("id", senderId);
            //body.put("message", msgStr);
            //response.setBody(body.toJSONString());
            response.setOKTitle();
            response.getHeaders().put("Content-Type", "application/json");
            return response;
        } catch (ParseException e) {
            throw new ServerException(400, "Can't parse body(json)");
        }
    }

    private long findSender(String token) throws ServerException {
        synchronized (users) {
            for (User user : users) {
                if (user.getToken().equals(token)) {
                    return user.getId();
                }
            }
        }
        throw new ServerException(500, "Can't find user");
    }

}
