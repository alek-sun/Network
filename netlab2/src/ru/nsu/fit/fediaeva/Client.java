package ru.nsu.fit.fediaeva;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client {
    private Socket socket;
    private File file;

    public Client(String path) {
        Path filePath = Paths.get(path);
        file = new File(filePath.toUri());
        socket = new Socket();
    }

    public void connect(String serverIP, int serverPort) throws IOException {
        try {
            socket.connect(new InetSocketAddress(serverIP, serverPort));
            sendFile();
            System.out.println(recvResponse());
        } catch (ReadingException e) {
            socket.close();
            System.out.println(e.getMessage());
        } catch (ConnectException e){
            socket.close();
            System.out.println("Server not found");
        } catch (IOException e) {
            socket.close();
            System.out.println("Sending data error");
            e.printStackTrace();
        }
    }

    private String recvResponse() throws IOException, ReadingException {
        BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
        byte[] respLenByte = new byte[4];
        if (in.read(respLenByte,0,4) != 4) {
            throw new ReadingException("Reading response length error");
        }
        int respLen = ByteBuffer.wrap(respLenByte).getInt();
        byte[] byteResponse = new byte[respLen];
        if (in.read(byteResponse, 0, respLen) != respLen){
            throw new ReadingException("Reading response error");
        }
        return new String(byteResponse, "UTF-8");
    }

    private void sendFile() throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

        byte[] bytes = file.getName().getBytes("UTF-8");
        int fileNameLen = bytes.length;
        byte[] fileNameLenByte = ByteBuffer.allocate(4).putInt(fileNameLen).array();    //  len of the name int = 4 byte
        out.write(fileNameLenByte);     //  send file name length
        out.write(bytes);    //  send file name

        byte[] data = new byte[256];
        FileInputStream fin = new FileInputStream(file);
        int c;
        while ((c = fin.read(data, 0, 256)) != -1){
            byte[] length = ByteBuffer.allocate(4).putInt(c).array();    //  len of the len :) long = 8 byte
            out.write(length);  // send part of file length
            out.write(data,0, c);
        }
        out.flush();
        socket.shutdownOutput();
    }
}
