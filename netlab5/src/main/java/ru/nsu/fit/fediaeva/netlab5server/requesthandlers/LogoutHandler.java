package ru.nsu.fit.fediaeva.netlab5server.requesthandlers;

import org.json.simple.JSONObject;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.User;

import java.util.ArrayList;

public class LogoutHandler implements RequestHandler {
    private final ArrayList<User> users;
    private final ArrayList<Message> messages;

    public LogoutHandler(ArrayList<User> users, ArrayList<Message> messages) {
        this.users = users;
        this.messages = messages;
    }

    @Override
    public Message getResponse(Message request) {
        String token = request.getToken();
        User deleted = null;
        synchronized (users) {
            for (User u : users) {
                if (u.getToken().equals(token)){
                    u.setOnline(false);
                    deleted = u;
                    break;
                }
            }
        }
        if (deleted != null) {
            synchronized (messages) {
                messages.add(new Message("Exit user : " + deleted.getName(), deleted.getId()));
            }
        }
        Message response = new Message();
        response.setOKTitle();
        JSONObject responseBody = new JSONObject();
        responseBody.put("message", "bye!");
        String bodyStr = responseBody.toJSONString();
        response.getHeaders().put("Content-Type", "application/json");
        response.setBody(bodyStr);
        return response;
    }
}
