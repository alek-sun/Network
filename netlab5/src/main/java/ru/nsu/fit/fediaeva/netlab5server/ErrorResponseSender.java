package ru.nsu.fit.fediaeva.netlab5server;

import java.io.BufferedWriter;
import java.io.IOException;

public class ErrorResponseSender implements Runnable {
    private String error;
    private String info;
    private BufferedWriter outputStream;

    ErrorResponseSender(String error, String info, BufferedWriter outputStream) {
        this.error = error;
        this.info = info;
        this.outputStream = outputStream;
    }

    public void run(){
        try {
            outputStream.write(
                    "HTTP/1.1 " + error + "\r\n" +
                    "Content-Length: " + info.length() + "\r\n" +
                    "Content-Type: text\r\n" +
                    "\r\n"
                    + info);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ignored) {
        }

    }
}
