package ru.nsu.fit.fediaeva.netlabs.netlab5;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

class WebSocketClient {
    private static CountDownLatch messageLatch;
    private ArrayBlockingQueue<String> messages;
    private String name;

    WebSocketClient(String name){
        messages = new ArrayBlockingQueue<>(20);
        messageLatch = new CountDownLatch(1);
        this.name = name;
    }

    void start(){
        ChatClientEndpoint clientEndpoint = new ChatClientEndpoint(messageLatch, messages, name);
        try {
            String message;
            ConsoleInputReadTask scanner = new ConsoleInputReadTask(messageLatch);

            while ((message = scanner.call()) != null) {
                messages.offer(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
