package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Scanner;

import util.NetworkUtil;

public class Server {
    static HashMap<String, User> users;
    static HashMap<Integer, Request> requests;
    final static int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 50, MAX_CHUNK_SIZE = 1048576, MIN_CHUNK_SIZE = 128;
    private static ServerSocket serverSocket;
    static int currentBufferSize = 0;

    public static void main(String[] args) {
        users = new HashMap<>();
        requests = new HashMap<>();
        try {
            Scanner scanner = new Scanner(new File("src/Server/users.txt"));
            int i = 0;
            while (scanner.hasNextLine()) {
                String name = scanner.nextLine();
                users.put(name, new User(name, i, null, false));
                i++;
            }
            scanner.close();
            Scanner scanner2 = new Scanner(new File("src/Server/Requests.txt"));
            while (scanner2.hasNextLine()) {
                String num = scanner2.nextLine();
                i = Integer.parseInt(num);
                String desc = scanner2.nextLine();
                String name = scanner2.nextLine();
                requests.put(i, new Request(i, desc, name));
            }
        } catch (FileNotFoundException e1) {
            File file1 = new File("src/Server/users.txt");
            File file2 = new File("src/Server/Requests.txt");
            try {
                if (!file1.exists())
                    file1.createNewFile();
                if (!file2.exists())
                    file2.createNewFile();
            } catch (Exception e) {
                System.out.println("Server throws:" + e);
            }
        }
        try {
            serverSocket = new ServerSocket(33333);
            while (true) {
                new ServerToClientThread(new NetworkUtil(serverSocket.accept()));
            }
        } catch (Exception e) {
            System.out.println("Server throws:" + e);
        }
    }
}
