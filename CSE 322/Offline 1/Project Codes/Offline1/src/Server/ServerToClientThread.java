package Server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import util.NetworkUtil;

public class ServerToClientThread implements Runnable {
    private Thread thread;
    private User user;
    private NetworkUtil connection;

    ServerToClientThread(NetworkUtil connection) {
        thread = new Thread(this);
        this.connection = connection;
        thread.start();
    }

    private void writeToFile(String s, String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.write(s);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean acceptFile() throws IOException, ClassNotFoundException {
        String fileType = (String) connection.read();
        String fileName = (String) connection.read();
        File file = new File("src/Server/Files/" + user.getId() + "/private/" + fileName);
        if (file.exists()) {
            connection.write("File already exists");
            return false;
        }
        file = new File("src/Server/Files/" + user.getId() + "/public/" + fileName);
        if (file.exists()) {
            connection.write("File already exists");
            return false;
        }
        connection.write("OK");
        int fileSize = (int) connection.read();
        if (fileSize + Server.currentBufferSize > Server.MAX_BUFFER_SIZE) {
            connection.write("File size too large");
            return false;
        } else {
            connection.write("OK");
            Server.currentBufferSize += fileSize;
            file = new File("src/Server/Files/" + user.getId() + "/" + fileType + "/" + fileName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            int chunkSize = new Random().nextInt(Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE) + Server.MIN_CHUNK_SIZE;
            connection.write(chunkSize);
            Object object;
            int readSize = 0;
            byte[] buffer = new byte[chunkSize];
            while ((object = connection.read()) instanceof byte[]) {
                buffer = (byte[]) object;
                int currentChunkSize = (int) connection.read();
                bos.write(buffer, 0, currentChunkSize);
                readSize += currentChunkSize;
            }
            if (object instanceof String) {
                if (((String) object).equalsIgnoreCase("Timeout")) {
                    System.out.println("Timeout");
                    file.delete();
                    Server.currentBufferSize -= fileSize;
                    bos.close();
                    return false;
                } else if (((String) object).equalsIgnoreCase("File upload successful")) {
                    if (fileSize != readSize) {
                        System.out.println("File upload unsuccessful");
                        file.delete();
                        Server.currentBufferSize -= fileSize;
                        connection.write("File upload unsuccessful");
                        bos.close();
                        return false;
                    } else {
                        connection.write("File upload successful");
                        bos.close();
                        Server.currentBufferSize -= fileSize;
                        return true;
                    }
                }
            }
            bos.close();
        }
        return false;
    }

    void sendFile() throws ClassNotFoundException, IOException {
        String name = (String) connection.read();
        String fileName = (String) connection.read();
        File file;
        if (name.equalsIgnoreCase(user.getName())) {
            file = new File("src/Server/Files/" + user.getId() + "/private/" + fileName);
            if (!file.exists()) {
                file = new File("src/Server/Files/" + user.getId() + "/public/" + fileName);
                if (!file.exists()) {
                    connection.write("File Does Not Exist");
                    return;
                }
            }
        } else {
            User target = Server.users.get(name);
            if (target == null) {
                connection.write("User does not exist");
                return;
            } else {
                file = new File("src/Server/Files/" + target.getId() + "/public/" + fileName);
                if (!file.exists()) {
                    connection.write("File Does Not Exist");
                    return;
                }
            }
        }
        connection.write("OK");
        connection.write(Server.MAX_CHUNK_SIZE);
        BufferedInputStream bos = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[Server.MAX_CHUNK_SIZE];
        int readSize;
        while ((readSize = bos.read(buffer)) != -1) {
            connection.write(buffer);
            connection.write(readSize);
        }
        bos.close();
        connection.write("File sent successfully");
    }

    void logOut() {
        user.setActive(false);
        try {
            connection.closeConnection();
        } catch (Exception e) {
            System.out.println("Error closing connection with " + user.getName());
        }

    }

    @Override
    public void run() {
        try {
            System.out.println("Got some connection req");
            String name = (String) connection.read();
            user = Server.users.get(name);
            while (user != null && user.isActive()) {
                System.out.println("User already online");
                connection.write("User already online");
                name = (String) connection.read();
                user = Server.users.get(name);
            }
            if (user == null) {
                user = new User(name, Server.users.size(), connection);
                writeToFile(name, "src/Server/users.txt");
                Server.users.put(name, user);
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
            System.out.println("Connected to " + user.getName());
            connection.write("Connected to server");
            while (true) {
                connection.write("You have " + user.getMessages().size() + " unread messages");
                String command = (String) connection.read();
                switch (command) {
                    case "getUsers":
                        connection.write(Server.users.size());
                        for (User user : Server.users.values()) {
                            if (user.isActive())
                                connection.write(user.getName() + "(Online)");
                            else
                                connection.write(user.getName() + "(Offline)");
                        }
                        break;
                    case "getOwnFileList":
                        File privateFile = new File("src/Server/Files/" + user.getId() + "/private");
                        File[] privateFiles = privateFile.listFiles();
                        connection.write(privateFiles.length);
                        for (File file : privateFiles) {
                            connection.write(file.getName());
                        }
                        File publicFile = new File("src/Server/Files/" + user.getId() + "/public");
                        File[] publicFiles = publicFile.listFiles();
                        connection.write(publicFiles.length);
                        for (File file : publicFiles) {
                            connection.write(file.getName());
                        }
                        if (((String) connection.read()).equalsIgnoreCase("downloadFile"))
                            sendFile();
                        break;
                    case "getOtherFileList":
                        User otherUser = Server.users.get((String) connection.read());
                        File otherPublicFile = new File("src/Server/Files/" + otherUser.getId() + "/public");
                        File[] otherPublicFiles = otherPublicFile.listFiles();
                        connection.write(otherPublicFiles.length);
                        for (File file : otherPublicFiles) {
                            connection.write(file.getName());
                        }
                        if (((String) connection.read()).equalsIgnoreCase("downloadFile"))
                            sendFile();
                        break;
                    case "makeRequest":
                        Random random = new Random();
                        int id = random.nextInt();
                        while (Server.requests.containsKey(id)) {
                            id = random.nextInt();
                        }
                        String description = (String) connection.read();
                        Request req = new Request(id, description, user.getName());
                        writeToFile(Integer.toString(id), "src/Server/Requests.txt");
                        writeToFile(description, "src/Server/Requests.txt");
                        writeToFile(user.getName(), "src/Server/Requests.txt");
                        Server.requests.put(id, req);
                        for (User user : Server.users.values()) {
                            user.addMessage(req.toString());
                        }
                        connection.write("Request made successfully");
                        break;
                    case "getRequests":
                        connection.write(Server.requests.size());
                        for (Request request : Server.requests.values()) {
                            connection.write(request.toString());
                        }
                        break;
                    case "fillRequest":
                        Request request = Server.requests.get((int) connection.read());
                        if (request == null) {
                            connection.write("Invalid request id");
                        } else {
                            connection.write("Send file now");
                            connection.read();
                            if (acceptFile())
                                Server.users.get(request.getUserName()).addMessage("Your request " + request.getReqId()
                                        + " has been fulfilled by " + user.getName());
                        }
                        break;
                    case "uploadFile":
                        acceptFile();
                        break;
                    case "downloadFile":
                        sendFile();
                        break;
                    case "getMessages":
                        connection.write(user.getMessages().size());
                        for (String message : user.getMessages()) {
                            connection.write(message);
                        }
                        user.getMessages().clear();
                        break;
                    case "logout":
                        logOut();
                        return;
                    case "doNothing":
                        break;
                }
            }
        } catch (IOException e1) {
            logOut();
            return;
        } catch (Exception e) {
            System.out.println("Write Failed to client " + user.getName() + " throws:" + e);
            e.printStackTrace();
        }
        // catch (SocketTimeoutException e){
        // System.out.println("Client "+user.getName()+" timed out");
        // user.setActive(false);
        // try {
        // connection.closeConnection();
        // } catch (IOException e1) {
        // System.out.println("Closing Connection to Client "+user.getName()+" throws:"
        // + e1);
        // }
        // }
    }

}
