import java.io.Serializable;

public class Message implements Serializable {
    public static final long serialVersionUID = 1L;
    private String MessageHeader;
    private String MessageBody;

    public Message(String msgHeader, String msgBody) { //Constructor
        this.MessageHeader = msgHeader;
        this.MessageBody = msgBody;
    }
    //Getters
    public String getMessageHead() {
        return MessageHeader;
    }
    public String getMessageBody() {
        return MessageBody;
    }

    @Override
    public String toString() { //Converts Object to a String
        return "Message(MessageHeader=" + MessageHeader + ", MessageBody=" + MessageBody + ")";
    }
}
