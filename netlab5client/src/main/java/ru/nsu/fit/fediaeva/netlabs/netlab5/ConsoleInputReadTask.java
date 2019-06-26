package ru.nsu.fit.fediaeva.netlabs.netlab5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class ConsoleInputReadTask implements Callable<String> {
    private CountDownLatch messageLatch;

    ConsoleInputReadTask(CountDownLatch messageLatch) {
        this.messageLatch = messageLatch;
    }

    public String call() throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        String input;
        do {
            try {
                while (!br.ready()) {
                    if (messageLatch.getCount() == 0) return null;
                    Thread.sleep(500);
                }
                if (messageLatch.getCount() == 0) return null;
                input = br.readLine();
            } catch (InterruptedException e) {
                System.out.println("ConsoleInputReadTask cancelled");
                return null;
            }
        } while (input.equals(""));
        return input;
    }
}