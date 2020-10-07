import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static Socket socket;
    static BufferedReader socketIn;
    private static PrintWriter out;

    public static void main(String[] args) throws Exception {
        Scanner userInput = new Scanner(System.in);

        System.out.println("What's the server IP? ");
        String serverip = userInput.nextLine();
        System.out.println("What's the server port? ");
        int port = userInput.nextInt();
        userInput.nextLine();

        socket = new Socket(serverip, port);
        socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        System.out.print("Chat sessions has started - enter a user name: ");
        String name = userInput.nextLine().trim();
        out.println(name); //out.flush();

        while (!(socketIn.readLine().trim().equals("SUBMITNAME"))) {
            System.out.println("Name isn't available or invalid. Please enter a new name:");
            name = userInput.nextLine().trim();
            out.println(name);
        }
        // start a thread to listen for server messages
        ClientServerHandler listener = new ClientServerHandler();
        Thread t = new Thread(listener);
        t.start();

        String line = userInput.nextLine().trim();
        while (!line.toLowerCase().startsWith("/quit")) {
            if (line.startsWith("@")) {
                String msg = String.format("PCHAT %s", line);
                out.println(msg);
                line = userInput.nextLine().trim();
            }
            else if (line.startsWith("*")) {
                String msg = String.format("REACT %s", line);
                out.println(msg);
                line = userInput.nextLine().trim();
            }
            else {
                String msg = String.format("CHAT %s", line);
                out.println(msg);
                line = userInput.nextLine().trim();
            }
        }
        out.println("QUIT");
        out.close();
        userInput.close();
        socketIn.close();
        socket.close();

    }
}