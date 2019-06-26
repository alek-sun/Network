package ru.nsu.fit.fediaeva.netlab5server.requesthandlers;

import org.json.simple.JSONObject;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.ServerException;
import ru.nsu.fit.fediaeva.netlab5server.User;

import java.util.ArrayList;

public class IdUserGetter implements RequestHandler{
    private final ArrayList<User> users;

    public IdUserGetter(ArrayList<User> users) {
        this.users = users;
    }

    @Override
    public Message getResponse(Message request) throws ServerException {
        JSONObject userInfo = new JSONObject();
        // path : users/<id>
        Integer id = Integer.parseInt(request.getPath().split("/")[1]);
        User user = getUser(id);
        if (user == null) {
            throw new ServerException(404, "No user with such id");
        }
        userInfo.put("id", id);
        userInfo.put("username", user.getName());
        userInfo.put("online", user.isOnline());
        String strBody = userInfo.toJSONString();

        Message response = new Message();

        response.getHeaders().put("Content-Type", "application/json");
        response.setOKTitle();
        response.setBody(strBody);
        return response;
    }

    private User getUser(Integer id) {
        synchronized (users) {
            for (User u : users) {
                if (u.getId() == id) {
                    return u;
                }
            }
        }
        return null;
    }
}
