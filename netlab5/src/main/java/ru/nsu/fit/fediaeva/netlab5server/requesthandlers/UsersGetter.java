package ru.nsu.fit.fediaeva.netlab5server.requesthandlers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.ServerException;
import ru.nsu.fit.fediaeva.netlab5server.User;

import java.util.ArrayList;

public class UsersGetter implements RequestHandler {
    private final ArrayList<User> users;

    public UsersGetter(ArrayList<User> users) {
        this.users = users;
    }

    @Override
    public Message getResponse(Message request) throws ServerException {
        JSONObject userList = new JSONObject();
        JSONArray usersArr = new JSONArray();
        synchronized (users) {
            users.forEach(u -> {
                JSONObject user = new JSONObject();
                user.put("id", u.getId());
                user.put("username", u.getName());
                user.put("online", u.isOnline());
                usersArr.add(user);
            });
        }
        userList.put("users", usersArr);
        String jsonContent = userList.toJSONString();

        Message response = new Message();
        response.getHeaders().put("Content-Type", "application/json");
        response.setOKTitle();
        response.setBody(jsonContent);
        return response;
    }
}
