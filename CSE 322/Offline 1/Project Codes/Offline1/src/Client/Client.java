package Client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Scanner;

import util.NetworkUtil;

public class Client {
    final static int PORT = 33333;
    final static String HOST = "127.0.0.1";

    static void downloadFile(NetworkUtil connection, Scanner scn, String name)
            throws IOException, ClassNotFoundException {
        System.out.println("Enter the file name:");
        String fileName = scn.next();
        System.out.println("Enter the file path to save the file to:");
        String filePath = scn.next();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Directory does not exist");
            connection.write("doNothing");
            return;
        }

        int num = 1;
        while (file.exists()) {
            file = new File(filePath + "/" + fileName + "(" + num + ")");
            num++;
        }
        connection.write("downloadFile");
        connection.write(name);
        connection.write(fileName);
        Object object = connection.read();
        if (((String) object).equalsIgnoreCase("File Does Not Exist")
                || ((String) object).equalsIgnoreCase("User does not exist")) {
            connection.write("doNothing");
            System.out.println((String) object);
            return;
        }
        int chunkSize = (int) connection.read();
        byte[] buffer = new byte[chunkSize];
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        while ((object = connection.read()) instanceof byte[]) {
            buffer = (byte[]) object;
            chunkSize = (int) connection.read();
            bos.write(buffer, 0, chunkSize);
        }
        System.out.println((String) object);
        bos.close();
    }

    static void uploadFile(NetworkUtil connection, Scanner scn, String fileType)
            throws IOException, ClassNotFoundException {
        System.out.println("Enter the file path:");
        String filePath = scn.next();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File does not exist");
            connection.write("doNothing");
            return;
        }
        connection.write("uploadFile");
        connection.write(fileType);
        connection.write(file.getName());
        String res = (String) connection.read();
        if (!res.equalsIgnoreCase("OK")) {
            System.out.println(res);
            return;
        }
        connection.write((int) file.length());
        if (((String) connection.read()).equalsIgnoreCase("File size too large")) {
            System.out.println("File size too large");
            return;
        }
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[(int) connection.read()];
        connection.setTimeOut(30000);
        try {
            int bytesRead = bis.read(buffer);
            while (bytesRead != -1) {
                connection.write(buffer);
                connection.write(bytesRead);
                bytesRead = bis.read(buffer);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("File upload failed");
            connection.write("Timeout");
            bis.close();
            connection.setTimeOut(0);
            return;
        }
        connection.write("File upload successful");
        bis.close();
        connection.setTimeOut(0);
        System.out.println((String) connection.read());
    }

    public static void main(String[] args) {
        System.out.println("Please Login/Sign Up using your username");
        Scanner scn = new Scanner(System.in);
        try {
            NetworkUtil connection = new NetworkUtil(HOST, PORT);
            String name = scn.nextLine();
            String res;
            connection.write(name);
            while (!((res = (String) connection.read()).equalsIgnoreCase("Connected to server"))) {
                System.out.println(res);
                System.out.println("User already online. Please use another id:");
                name = scn.nextLine();
                connection.write(name);
            }
            System.out.println("Connected to server");
            while (true) {
                res = (String) connection.read();
                System.out.println(res);
                System.out.println("Enter your command:");
                System.out.println(
                        "1. Get List of Users\n2. Look Up your files list\n3. Look Up a user's public files list\n4.Make a file request\n5.See all requests\n6.Fill a request\n7. Upload a file\n8. See all your messages\n9. Log Out & Exit");
                while (!scn.hasNextInt()) {
                    System.out.println("Please enter a valid command");
                    scn.next();
                }
                int command = scn.nextInt();
                switch (command) {
                    case 1:
                        connection.write("getUsers");
                        int userNumber = (int) connection.read();
                        System.out.println("There are " + userNumber + " users registered");
                        for (int i = 0; i < userNumber; i++)
                            System.out.println((String) connection.read());
                        break;
                    case 2:
                        connection.write("getOwnFileList");
                        int privateFileNumber = (int) connection.read();
                        System.out.println("You have " + privateFileNumber + " private files");
                        for (int i = 0; i < privateFileNumber; i++)
                            System.out.println((String) connection.read());
                        int publicFileNumber = (int) connection.read();
                        System.out.println("You have " + publicFileNumber + " public files");
                        for (int i = 0; i < publicFileNumber; i++)
                            System.out.println((String) connection.read());
                        if(privateFileNumber == 0 && publicFileNumber == 0){
                            connection.write("doNothing");
                            break;
                        }
                        System.out.println("Do you want to download any file? (y/n)");
                        String ans = scn.next();
                        if (ans.equals("y"))
                            downloadFile(connection, scn, name);
                        else {
                            connection.write("doNothing");
                        }
                        break;
                    case 3:
                        connection.write("getOtherFileList");
                        System.out.println("Enter the user name:");
                        String userName = scn.next();
                        connection.write(userName);
                        Object obj = connection.read();
                        if (obj instanceof String) {
                            System.out.println((String) obj);
                            break;
                        }
                        int otherPublicFileNumber = (int) obj;
                        System.out.println(userName + " has " + otherPublicFileNumber + " public files");
                        for (int i = 0; i < otherPublicFileNumber; i++)
                            System.out.println((String) connection.read());
                        if (otherPublicFileNumber == 0){
                            connection.write("doNothing");
                            break;
                        }
                        System.out.println("Do you want to download any file? (y/n)");
                        ans = scn.next();
                        if (ans.equals("y"))
                            downloadFile(connection, scn, userName);
                        else
                            connection.write("doNothing");
                        break;
                    case 4:
                        connection.write("makeRequest");
                        System.out.println("Enter the file description:");
                        String requestDesc = scn.next();
                        connection.write(requestDesc);
                        System.out.println((String) connection.read());
                        break;
                    case 5:
                        connection.write("getRequests");
                        int requestNumber = (int) connection.read();
                        System.out.println("There are " + requestNumber + " requests");
                        for (int i = 0; i < requestNumber; i++) {
                            System.out.println((String) connection.read());
                        }
                        break;
                    case 6:
                        connection.write("fillRequest");
                        System.out.println("Enter the request id:");
                        connection.write(scn.nextInt());
                        if (((String) connection.read()).equalsIgnoreCase("Invalid request id")) {
                            System.out.println("Invalid request id");
                            break;
                        }
                        uploadFile(connection, scn, "public");
                        connection.write("uploadFile");
                        break;
                    case 7:
                        System.out.println("Is Your file public? (y/n)");
                        String fileType = scn.next();
                        if (fileType.equals("y"))
                            uploadFile(connection, scn, "public");
                        else
                            uploadFile(connection, scn, "private");
                        break;
                    case 8:
                        connection.write("getMessages");
                        int messageNumber = (int) connection.read();
                        System.out.println("You have " + messageNumber + " unread messages");
                        for (int i = 0; i < messageNumber; i++)
                            System.out.println((String) connection.read());
                        break;
                    case 9:
                        connection.write("logOut");
                        System.out.println("Logged out successfully");
                        connection.closeConnection();
                        System.exit(0);
                        break;
                }
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        scn.close();
    }
}
