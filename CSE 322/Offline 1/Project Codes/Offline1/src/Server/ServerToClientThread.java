package Server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import util.NetworkUtil;

public class ServerToClientThread implements Runnable {
    private Thread thread;
    private User user;
    private NetworkUtil connection;

    ServerToClientThread(User user) {
        this.user = user;
        thread = new Thread(this);
        connection = user.getNetworkUtil();
        thread.start();
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
                if ((String) object == "Timeout") {
                    System.out.println("Timeout");
                    file.delete();
                    Server.currentBufferSize -= fileSize;
                    bos.close();
                    return false;
                } else if ((String) object == "File upload successful") {
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
        if (name == user.getName()) {
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
            System.out.println("Connected to " + user.getName());
            connection.write("Connected to server");
            while (true) {
                connection.write("You have " + user.getMessages().size() + "unread messages");
                String command = (String) connection.read();
                switch (command) {
                    case "getUsers":
                        connection.write(Server.users.size());
                        for (User user : Server.users.values()) {
                            connection.write(user.getName());
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
                        break;
                    case "getOtherFileList":
                        User otherUser = Server.users.get((String) connection.read());
                        File otherPublicFile = new File("src/Server/Files/" + otherUser.getId() + "/public");
                        File[] otherPublicFiles = otherPublicFile.listFiles();
                        connection.write(otherPublicFiles.length);
                        for (File file : otherPublicFiles) {
                            connection.write(file.getName());
                        }
                        break;
                    case "makeRequest":
                        Random random = new Random();
                        int id = random.nextInt();
                        while (Server.requests.containsKey(id)) {
                            id = random.nextInt();
                        }
                        Request req = new Request(id, (String) connection.read(), user.getName());
                        Server.requests.put(id, req);
                        for (User user : Server.users.values()) {
                            user.addMessage(req.toString());
                        }
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
