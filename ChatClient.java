import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static Socket socket;
    private static ObjectInputStream socketIn;
    private static ObjectOutputStream out;
    
    public static void main(String[] args) throws Exception {
        Scanner userInput = new Scanner(System.in);

        System.out.println("What's the server IP? ");
        String serverip = userInput.nextLine();
        System.out.println("What's the server port? ");
        int port = userInput.nextInt(); //Typically 54321, check it out before entering, since it may be different from ChatServer.java
        userInput.nextLine();

        socket = new Socket(serverip, port);
        socketIn = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());

        Message inMessage = (Message) socketIn.readObject(); //Using the new Message Object
        while (!(inMessage.getMessageHead().equals("SUBMITNAME"))) {
            System.out.println(inMessage);
        }
        System.out.print("Chat session has started - enter a user name: ");
        String nameIn= userInput.nextLine().trim();
        while (nameIn.equals("") || nameIn.contains(" ")) { //Users cannot be empty strings or contain any spaces
            System.out.print("INVALID USERNAME");
            nameIn = userInput.nextLine().trim();
        }
        Message msg = new Message("SUBMITNAME", nameIn);
        out.writeObject(msg);
        while (((Message) socketIn.readObject()).getMessageHead().equals("SUBMITNAME")) { //While the server sends SUBMITNAME... Keep on entering until it is valid
            System.out.print("Name Unavailable, choose a new one: ");
            nameIn = userInput.nextLine().trim();
            while (nameIn.equals("") || nameIn.contains(" ")) {
                System.out.println("INVALID USERNAME. Name cannot be empty or contain any spaces: ");
                nameIn = userInput.nextLine().trim();
            }
            out.writeObject(new Message("SUBMITNAME", nameIn));
        }
        ClientServerHandler listener = new ClientServerHandler(socketIn); //start of the listener process
        Thread t = new Thread(listener);
        t.start();

        String line = userInput.nextLine().trim();
        while(!line.toLowerCase().startsWith("/quit")) { //Keep on going until the user enters /quit
            if(line.charAt(0) == '@') {
                String[] message = line.split(" "); //List to contain all the different people originally separated by spaces
                int subStringIndex = 0;
                int index = 0;
                while (message[index].charAt(0) == '@'){
                    subStringIndex += message[index].length() + 1;
                    index++;
                }
                String recipients = line.substring(0, subStringIndex).trim();
                String chat = line.substring(subStringIndex).trim();
                if(chat.length() == 0){
                    System.out.println("Empty messages cannot be sent");
                }
                Message chatMessage = new Message("PCHAT", (subStringIndex + Integer.toString(subStringIndex).length() + 1) + " " + recipients + " " + chat);
                out.writeObject(chatMessage); //send the "complicated" PCHAT message containing the recipients and the chat
            }
            else if(line.equals("/whoishere")){
                System.out.println(listener.users);
            }
            else{
                out.writeObject(new Message("CHAT", line));
            }
            line = userInput.nextLine().trim();
        }
        out.writeObject(new Message("QUIT", "")); //Finishing closing items
        out.close();
        userInput.close();
        socketIn.close();
        socket.close();
    }
}
