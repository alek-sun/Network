package ru.nsu.fit.fediaeva.netlab3;

public class Main {

    public static void main(String[] args) {
        TreeList list;
        if (args.length == 3) {
            list = new TreeList(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        } else if (args.length == 5){
            list = new TreeList(args[0], Integer.parseInt(args[1]),
                    args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } else {
            System.out.println("Enter arguments by one of this format:\n" +
                    "for root : <IP> <port> <lost percent>\nfor other lists : <IP> <port> <parent IP> <parent port> <lost percent>");
            return;
        }
        list.start();
    }
}
