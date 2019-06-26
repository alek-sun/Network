package netlab5.server;

class Message {
    private User sender;
    private String message;

    Message(User sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    User getSender() {
        return sender;
    }

    String getData() {
        return message;
    }
}
