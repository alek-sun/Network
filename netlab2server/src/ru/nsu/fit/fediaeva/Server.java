package ru.nsu.fit.fediaeva;

import ru.nsu.fit.fediaeva.threadpool.SocketTask;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    private ServerSocket socket;
    private Timer timer;

    public Server(int port) throws IOException {
        socket = new ServerSocket(port);
        timer = new Timer(3);
    }

    public void listen(){
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(10);
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 3, 0, TimeUnit.SECONDS, queue);
        threadPool.execute(timer);
        while (true) {
            try {
                Socket s = socket.accept();
                SocketTask task = new SocketTask(s);
                timer.addClient(task);
                threadPool.execute(task);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
