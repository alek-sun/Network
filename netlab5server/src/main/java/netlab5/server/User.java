package netlab5.server;

import javax.websocket.Session;

class User {
    private static int globalId = 0;
    private Session session;
    private int id;
    private String name;

    User(Session session, String name) {
        this.session = session;
        this.name = name;
        id = globalId;
        globalId++;
    }

    Session getSession() {
        return session;
    }

    int getId() {
        return id;
    }

    String getName() {
        return name;
    }

}
