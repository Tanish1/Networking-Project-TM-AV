import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServer { //Almost identical file to the one in OneDrive
    public static final int PORT = 54320; //I cannot use 54321 for some reason, even restarted my laptop but no luck, so make sure to change port to 54320, or change this
    private static final HashMap<String, ClientConnectionData> clientHashMap = new HashMap<>();
    private static final ArrayList<ClientConnectionData> clientList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(100);

        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Chat Server started.");
            System.out.println("Local IP: " + Inet4Address.getLocalHost().getHostAddress());
            System.out.println("Local Port: " + serverSocket.getLocalPort());
        
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    System.out.printf("Connected to %s:%d on local port %d\n", socket.getInetAddress(), socket.getPort(), socket.getLocalPort());

                    String name = socket.getInetAddress().getHostName();
                    ClientConnectionData client = new ClientConnectionData(socket, in, out, name);
                    pool.execute(new ServerClientHandler(client, clientList, clientHashMap));
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } 
    }
}
