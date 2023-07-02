package Server;

import java.io.File;
import java.net.ServerSocket;
import java.util.HashMap;

import util.NetworkUtil;

public class Server {
    static HashMap<String, User> users;
    static HashMap<Integer, Request> requests;
    final static int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 50, MAX_CHUNK_SIZE = 1048576, MIN_CHUNK_SIZE = 128;
    private static ServerSocket serverSocket;
    static int currentBufferSize = 0;

    public static void main(String[] args) {
        users = new HashMap<>();
        try {
            serverSocket = new ServerSocket(33333);
            while (true) {
                NetworkUtil connection = new NetworkUtil(serverSocket.accept());
                System.out.println("Got some connection req");
                String name = (String) connection.read();
                User user = users.get(name);
                while (user != null && user.isActive()) {
                    System.out.println("User already online");
                    connection.write("User already online");
                    name = (String) connection.read();
                    user = users.get(name);
                }
                if (user == null) {
                    user = new User(name, users.size(), connection);
                    users.put(name, user);
                    File file = new File("src/Server/Files/" + user.getId() + "/public");
                    while (!file.mkdirs())
                        ;
                    file = new File("src/Server/Files/" + user.getId() + "/private");
                    while (!file.mkdir())
                        ;
                } else {
                    user.setNetworkUtil(connection);
                    user.setActive(true);
                }
                new ServerToClientThread(user);
            }
        } catch (Exception e) {
            System.out.println("Server throws:" + e);
        }
    }
}
