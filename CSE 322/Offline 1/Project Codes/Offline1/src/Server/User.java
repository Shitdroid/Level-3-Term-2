package Server;

import java.util.Vector;
import util.NetworkUtil;
public class User {
    private Vector<String> messages;
    private NetworkUtil networkUtil;
    private int id;
    private String name;
    private boolean active;
    public User(String name,int id,NetworkUtil networkUtil) {
        this.networkUtil = networkUtil;
        this.id = id;
        this.name = name;
        this.active = true;
        this.messages = new Vector<>();
    }
    public Vector<String> getMessages() {
        return messages;
    }
    public void setMessages(Vector<String> messages) {
        this.messages = messages;
    }
    public void addMessage(String message) {
        this.messages.add(message);
    }
    public void clearMessages() {
        this.messages.clear();
    }
    public NetworkUtil getNetworkUtil() {
        return networkUtil;
    }
    public void setNetworkUtil(NetworkUtil networkUtil) {
        this.networkUtil = networkUtil;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }


    
}
