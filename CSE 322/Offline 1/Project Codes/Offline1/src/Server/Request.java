package Server;

import java.io.Serializable;

public class Request implements Serializable{
    private int reqId;
    private String reqDescription, userName;
    public Request(int reqId, String reqDescription, String userName) {
        this.reqId = reqId;
        this.reqDescription = reqDescription;
        this.userName = userName;
    }
    public int getReqId() {
        return reqId;
    }
    public String getReqDescription() {
        return reqDescription;
    }
    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        return "Request{" + "Id=" + reqId + ", Description=" + reqDescription + ", from user=" + userName + '}';
    }
}
