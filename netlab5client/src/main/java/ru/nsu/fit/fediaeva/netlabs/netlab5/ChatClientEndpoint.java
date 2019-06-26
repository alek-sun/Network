package ru.nsu.fit.fediaeva.netlabs.netlab5;

import org.glassfish.tyrus.client.ClientManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ChatClientEndpoint extends Endpoint {
    private CountDownLatch latch;
    private ArrayBlockingQueue<String> messages;
    private Session curSession;
    private String name;

    ChatClientEndpoint(CountDownLatch latch, ArrayBlockingQueue<String> messages, String name) {
        this.latch = latch;
        this.messages = messages;
        this.name = name;

        final ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();
        ClientManager client = ClientManager.createClient();

        try {
            client.connectToServer(this, config,
                    new URI("ws://localhost:8025/websockets/login"));
        } catch (URISyntaxException | IOException | DeploymentException e) {
            e.printStackTrace();
        }

    }

    private void handleMessage(String type, String messageBody) {
        switch (type) {
            case ("usual"): {
                System.out.println(messageBody);
                break;
            }
            case ("login"): {
                Thread senderThread = new Thread(new Sender(messages, curSession, latch));
                senderThread.start();
                System.out.println("Successful connected");
                break;
            }
            case ("logout"): {
                System.out.println(messageBody);
                latch.countDown();
                break;
            }
            case ("msglist"): {
                StringBuilder res = new StringBuilder();
                res.append("====== MESSAGE LIST =======\r\n");
                JSONParser parser = new JSONParser();
                try {
                    JSONObject body = (JSONObject) parser.parse(messageBody);
                    JSONArray msgArr = (JSONArray) body.get("messages");
                    for (Object msg : msgArr) {
                        JSONObject msgobj = (JSONObject) msg;
                        res.append(msgobj.get("sender")).append(" : ").append(msgobj.get("data")).append("\r\n");
                    }
                    System.out.println(res);
                } catch (ParseException e) {
                    System.out.println("Parse message error");
                }
                break;
            }
            case ("userlist"): {
                StringBuilder res = new StringBuilder();
                res.append("====== USER LIST =======\r\n");
                JSONParser parser = new JSONParser();
                try {
                    JSONObject body = (JSONObject) parser.parse(messageBody);
                    JSONArray userArr = (JSONArray) body.get("users");
                    for (Object msg : userArr) {
                        JSONObject userObj = (JSONObject) msg;
                        res.append(userObj.toJSONString()).append("\r\n");
                    }
                    System.out.println(res);
                } catch (ParseException e) {
                    System.out.println("Parse message error");
                }
            }
        }
    }


    @Override
    public void onOpen(Session session, EndpointConfig config) {
        RemoteEndpoint.Async remote = session.getAsyncRemote();
        curSession = session;
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                String[] typeMsg = message.split("%%", 2);
                if (typeMsg.length < 2) {
                    System.out.println("Server error");
                    return;
                }
                handleMessage(typeMsg[0], typeMsg[1]);
            }
        });
        remote.sendText("login%%" + name);
    }
}