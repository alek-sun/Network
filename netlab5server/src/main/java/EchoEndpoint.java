import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/login")
public class EchoEndpoint {

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("onopen");
        session.getBasicRemote().sendText("onOpen");
    }

    @OnMessage
    public String echo(String message) {
        System.out.println("onmsg");
        return message + " (from your server)";
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {

    }
}
