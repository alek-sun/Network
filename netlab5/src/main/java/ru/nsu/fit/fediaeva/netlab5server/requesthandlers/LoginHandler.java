package ru.nsu.fit.fediaeva.netlab5server.requesthandlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.ServerException;
import ru.nsu.fit.fediaeva.netlab5server.User;

import java.util.ArrayList;

public class LoginHandler implements RequestHandler {
    private final ArrayList<User> users;
    private final ArrayList<Message> messages;

    public LoginHandler(ArrayList<User> users, ArrayList<Message> messages) {
        this.users = users;
        this.messages = messages;
    }

    @Override
    public Message getResponse(Message request) throws ServerException {
        JSONParser jsonParser = new JSONParser();
        try {
            Message response = new Message();
            JSONObject reqBody = (JSONObject) jsonParser.parse(request.getBody());
            String username = (String) reqBody.get("username");
            User equal;
            if ((equal = findEquals(username)) != null){
                if (equal.isOnline()) {
                    response.setTitle("HTTP/1.1 401 Unauthorized");
                    response.getHeaders().put("WWW-Authenticate", "Token realm='Username is already in use'");
                    return response;
                } else {
                    equal.setOnline(true);
                    synchronized (messages) {
                        messages.add(new Message("User " + username + " online again", equal.getId()));
                    }
                    response.setOKTitle();
                    JSONObject responseBody = new JSONObject();
                    responseBody.put("id", equal.getId());
                    responseBody.put("username", equal.getName());
                    responseBody.put("online", equal.isOnline());
                    responseBody.put("token", equal.getToken());
                    String bodyStr = responseBody.toJSONString();
                    response.getHeaders().put("Content-Type", "application/json");
                    response.setBody(bodyStr);
                    return response;
                }
            }
            User user = new User(username, true);
            synchronized (users) {
                users.add(user);
            }
            synchronized (messages) {
                messages.add(new Message("New user : " + username, user.getId()));
            }
            response.setOKTitle();
            JSONObject responseBody = new JSONObject();
            responseBody.put("id", user.getId());
            responseBody.put("username", user.getName());
            responseBody.put("online", user.isOnline());
            responseBody.put("token", user.getToken());
            String bodyStr = responseBody.toJSONString();
            response.getHeaders().put("Content-Type", "application/json");
            response.setBody(bodyStr);
            return response;
        } catch (ParseException e) {
            throw new ServerException(400, "Can't parse body(json)");
        }
    }

    private User findEquals(String username) {
        synchronized (users) {
            for (User u : users) {
                if (u.getName().equals(username)) {
                    return u;
                }
            }
        }
        return null;
    }
}
