import java.io.ObjectInputStream;
import java.net.SocketException;

public class ClientServerHandler implements Runnable { //decided to put all the functions here rather than in ChatClient
    ObjectInputStream socketIn;
    String users;

    public ClientServerHandler(ObjectInputStream socketIn) {
        this.socketIn = socketIn;
        users = "";
    }
    @Override
        public void run() {
        try {
            boolean UserNamePrints = true;
            Message incomingMessage;

            while ((incomingMessage = (Message) socketIn.readObject()) != null) { //While input isn't null...
                String MessageBody = incomingMessage.getMessageBody().trim();
                String MessageHead = incomingMessage.getMessageHead().trim();

                if (MessageHead.equals("WELCOME")) {
                    System.out.println(MessageBody + " has joined.");
                }
                else if (MessageHead.equals("CHAT")) { //A regular Chat to all the clients
                    String username = MessageBody.substring(0, MessageBody.indexOf(' '));
                    String chat = MessageBody.substring(username.length()).trim();
                    System.out.println(username + ": " + chat);
                }
                else if (MessageHead.equals("PCHAT")) { //Sends a private chat, whether with a lot or one person
                    String[] message = incomingMessage.getMessageBody().split(" ");
                    String username = message[0];
                    String chat = incomingMessage.getMessageBody().substring(username.length()).trim();
                    System.out.println(username + " (private): " + chat);
                }
                else if (MessageHead.equals("EXIT")) { //Exits
                    String name = incomingMessage.getMessageBody();
                    System.out.println(name + " has left.");
                }
                else if (MessageHead.equals("USERS")) { //Prints all users
                    users = MessageBody;
                    if (UserNamePrints) {
                        System.out.println(users);
                        UserNamePrints = false;
                    }
                }
                else if (incomingMessage.getMessageHead().equals("NOUSER")) { //Tells the client if the userName they entered doesn't exist
                    String recipient = incomingMessage.getMessageBody();
                    System.out.println("Username \"" + recipient + "\" does not exist.");
                }
                else if (MessageHead.equals("LEFT")) {
                    break;
                }
                else {
                    System.out.println("Unknown message from server");
                }
            }
        }
        catch (SocketException ex) {
        }
        catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        }
        finally {
            System.out.println("Client Listener exiting");
        }
    }
}
