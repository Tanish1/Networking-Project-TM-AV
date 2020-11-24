import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnectionData {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream out;
    private String name;
    private String userName;

    public ClientConnectionData(Socket socket, ObjectInputStream input, ObjectOutputStream out, String name) { //Constructor
        this.socket = socket;
        this.input = input;
        this.out = out;
        this.name = name;
    }
//Getters and Setters
    public ObjectInputStream getInput() {
        return input;
    }
    public ObjectOutputStream getOut() {
        return out;
    }
    public String getName() {
        return name;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
