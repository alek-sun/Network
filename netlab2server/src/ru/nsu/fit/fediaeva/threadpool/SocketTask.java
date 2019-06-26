package ru.nsu.fit.fediaeva.threadpool;

import ru.nsu.fit.fediaeva.ReceiveException;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SocketTask implements Runnable {
    private Socket socket;
    private boolean isSuccessful;
    private boolean isCompleted;
    private Long globalCountData;
    private Long lastCountData;
    private Long startTime;


    public SocketTask(Socket s) {
        socket = s;
        isSuccessful = true;
        globalCountData = 0L;
        lastCountData = 0L;
        isCompleted = false;
        startTime = System.currentTimeMillis();
    }

    public String getIP(){
        return socket.getInetAddress().toString();
    }

    public void setLastCountData(Long lastCountData) {
        this.lastCountData = lastCountData;
    }

    @Override
    public void run() {
        try {
            receiveRequest();
        } catch (ReceiveException e) {
            isSuccessful = false;
            System.out.println(e.getMessage());
        }
        isCompleted = true;
        try {
            sendResponse();
        } catch (IOException e) {
            System.out.println("Fatal sending response error/closing socket error");
        }
    }

    private void sendResponse() throws IOException {
        OutputStream os = socket.getOutputStream();
        int len = isSuccessful? "success".getBytes().length : "failure".getBytes().length;
        try {
            os.write(ByteBuffer.allocate(4).putInt(len).array());
            os.write((isSuccessful? "success" : "failure").getBytes("UTF-8"));
            os.flush();
        } catch (IOException e) {
            System.out.println("Sending response error");
        }
        os.close();
    }

    private void receiveRequest() throws ReceiveException {
        String fileName;
        BufferedInputStream in;
        try {
            byte[] nameLenBuf = new byte[4];
            in = new BufferedInputStream(socket.getInputStream());
            if (in.read(nameLenBuf, 0, 4) != 4) { // read file name length
                throw new ReceiveException("Reading file name length error");
            }
            globalCountData += 4;
            Integer nameLen = ByteBuffer.wrap(nameLenBuf).getInt();

            byte[] fileNameBuf = new byte[nameLen];
            if (in.read(fileNameBuf, 0, nameLen) != nameLen) {   //  read file name
                throw new ReceiveException("Reading file name error");
            }
            globalCountData += nameLen;
            try {
                fileName = new String(fileNameBuf, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new ReceiveException("Unsupported encoding");
            }

            recvFileData(fileName, in);
        } catch (IOException e) {
            throw new ReceiveException("Reading error");
        }
        try {
            socket.shutdownInput();
        } catch (IOException e) {
            System.out.println("Socket input hasn't been closed");
        }
    }

    private void recvFileData(String fileName, BufferedInputStream in) throws ReceiveException {
        Path p = Paths.get("./uploads");
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            System.out.println("Create dir error");
        }
        if (fileName.contains(".." + File.separator)){
            fileName = fileName.replaceAll(File.separator, "");
        }
        File f = new File("./uploads" + File.separator + fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException ignored){}

        int curReadCount;
        byte[] fileData = new byte[256];
        byte[] filePartLenBuf = new byte[4];
        try {
            while ((curReadCount = in.read(filePartLenBuf, 0, 4)) != -1) {
                if (curReadCount != 4) {   //  read part of file length
                    throw new ReceiveException("Reading file length error");
                }
                globalCountData += 4;
                Integer filePartLen = ByteBuffer.wrap(filePartLenBuf).getInt();
                curReadCount = in.readNBytes(fileData, 0, filePartLen);
                if (curReadCount != filePartLen) {
                    throw new ReceiveException("Reading file error");
                }
                globalCountData += curReadCount;
                try {
                    fos.write(fileData, 0, curReadCount);
                } catch (IOException e) {
                    System.out.println("Writing file error");
                }
            }
        } catch (IOException e){
            throw new ReceiveException("Reading file error");
        }
        try {
            fos.close();
        } catch (IOException e) {
            System.out.println("File hasn't been closed");
        }
    }

    public Long getGlobalCountData() {
        return globalCountData;
    }

    public Long getLastCountData() {
        return lastCountData;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public Long getStartTime() {
        return startTime;
    }
}

