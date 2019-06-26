package ru.nsu.fit.fediaeva.netlab5server;

import ru.nsu.fit.fediaeva.netlab5server.requesthandlers.RequestHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class ResponseSender implements Runnable {
    private BufferedInputStream in;
    private BufferedWriter outputStream;
    private final ArrayList<User> users;
    private RequestHandler handler;
    private Message request;

    ResponseSender(BufferedInputStream in,
                   OutputStream outputStream,
                   ArrayList<User> users,
                   RequestHandler handler,
                   Message httpMessage) {
        this.in = in;
        this.outputStream = new BufferedWriter(new OutputStreamWriter(outputStream));
        this.users = users;
        this.handler = handler;
        this.request = httpMessage;
    }

    @Override
    public void run() {
        try {
            parseMessage();
            updateUserTime();
            checkMessage();
            request.print();
            sendJsonResponse(handler.getResponse(request));
        } catch (ServerException e) {
            System.out.println(">>>>>> " + e.getTitle());
            System.out.println(e.getInfo());
            ErrorResponseSender errSender = new ErrorResponseSender(e.getTitle(), e.getInfo(), outputStream);
            errSender.run();
        }
    }

    private void updateUserTime() {
        synchronized (users) {
            for (User u : users) {
                if (u.getToken().equals(request.getToken())){
                    u.setLastMsgTime(System.currentTimeMillis());
                }
            }

        }
    }

    private void checkMessage() throws ServerException {
        if (!request.getPath().equals("login")) {
            if (!request.getHeaders().containsKey("Authorization")) {
                throw new ServerException(401, "Authorisation token is absent");
            }
            String token = request.getToken();
            if (!isUserToken(token)) {
                throw new ServerException(403, "Unknown authorisation token");
            }
        }
        if (!request.getHeaders().get("Content-Type").equals("application/json")) {
            throw new ServerException(400, "Unknown message format");
        }
    }

    private boolean isUserToken(String token) {
        synchronized (users) {
            for (User u : users) {
                if (u.getToken().equals(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void parseMessage() throws ServerException {
        /*HashMap<String, String> headers = new HashMap<>();
        String line;
        while (!(line = Server.readBufferedLine(in)).equals("")) {
            String[] kv = line.split(":", 2);
            headers.put(kv[0], kv[1].replaceFirst(" ", ""));
        }
        int len;
        request.getHeaders().putAll(headers);
        if (request.getHeaders().containsKey("Content-Length")) {
            len = Integer.parseInt(request.getHeaders().get("Content-Length"));
        } else {
            throw new ServerException(400, "Content-Length is absent");
        }
        byte[] contentBuf = new byte[len];
        int n;
        try {
            n = in.read(contentBuf, 0, len);
        } catch (IOException e) {
            throw new ServerException(500, "Reading body error");
        }
        if (n != len) {
            throw new ServerException(500, "Not whole request body has been read");
        }
        request.setBody(new String(contentBuf, StandardCharsets.UTF_8));*/
    }

    private void sendJsonResponse(Message response) throws ServerException {
        try {
            outputStream.write(response.getTitle() + "\r\n");
            outputStream.write(response.getHeadersString());
            outputStream.write(response.getBody());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new ServerException(500, "Writing response error");
        }

    }

}


