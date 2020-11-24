import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerClientHandler implements Runnable {
    ClientConnectionData client;
    HashMap<String, ClientConnectionData> clientHashMap;
    ArrayList<ClientConnectionData> clientList;

    public ServerClientHandler(ClientConnectionData client, ArrayList<ClientConnectionData> clientList, HashMap<String, ClientConnectionData> clientHashMap) { //Constructor
        this.client = client;
        this.clientList = clientList;
        this.clientHashMap = clientHashMap;
    }

    private boolean userNameInClientList(final String userName) { //Checks if the userName is contained in the clientList (HashMap for more efficiency and easier)
        return clientHashMap.containsKey(userName);
    }

    HashMap reactionHash = new HashMap<String, String>();

    public void broadcast(Message msg) { //Broadcasts to ALL clients, including the one that sent it
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    c.getOut().writeObject(msg);
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void broadCastOther(Message msg) { //Broadcasts the message without sending it to the client that sent it
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if(c == client){
                        continue;
                    }
                    c.getOut().writeObject(msg);
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void sendPrivateChat (Message msg, String recipient) { //Sends a private chat to a client defined by the sender
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientHashMap) {
                clientHashMap.get(recipient).getOut().writeObject(msg);
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = client.getInput();
            ObjectOutputStream out = client.getOut();

            String userName;
            do { //does the operations first, then continues on while (I miss Freshman year)
                out.writeObject(new Message("SUBMITNAME", ""));
                Message userMessage = (Message) in.readObject(); //Cast to a Message Object
                userName = userMessage.getMessageBody();
            } while (userNameInClientList(userName));
            System.out.println(userName);

            synchronized (clientList) { //adds the client to ArrayList
                client.setUserName(userName);
                clientList.add(client);
            }
            synchronized (clientHashMap) { //adds the client to HashMap
                clientHashMap.put(userName, client);
            }

            System.out.println("added client " + client.getName()); //notify all clients of the new client, welcomes them, and tells the clients all the users
            broadcast(new Message("WELCOME", client.getUserName()));
            broadcast(new Message("USERS", userString()));

            reactionAdder(reactionHash);

            Message incoming;
            while((incoming = (Message) in.readObject()) != null) {
                String msgHeader = incoming.getMessageHead();
                String msgBody = incoming.getMessageBody();
                switch (msgHeader) { //Almost forgot about this method, much easier than if/else and thank to Alex for reminding me
                    case "CHAT": { //if msgHeader is CHAT
                        if (msgBody.length() > 0) {
                            Message msg = new Message("CHAT", client.getUserName()+ " " + msgBody);
                            broadCastOther(msg);
                        }
                        break;
                    }
                    case "PCHAT": {
                        String[] tokenized = msgBody.split(" ");
                        int chatBeginsAt = Integer.parseInt(tokenized[0]);
                        String chat = msgBody.substring(chatBeginsAt).trim();
                        for (int i = 1; i < tokenized.length; i++) {
                            if(tokenized[i].charAt(0) != '@') break;
                            String recipient = tokenized[i].substring(1);
                            if (!userNameInClientList(recipient)) {
                                client.getOut().writeObject(new Message("NOUSER", recipient));
                                continue;
                            }
                            sendPrivateChat(new Message("PCHAT", client.getUserName() + " " + chat), recipient);
                        }
                        break;
                    }
                    case "REACT": { //Check this out this might be broken
                        String key = incoming.getMessageBody().substring(7).trim();
                        String reaction = (String) reactionHash.get(key);
                        if (reaction != null) {
                            String message = String.format("%s Reacted: %s", client.getUserName(), reaction);
                            Message msg = new Message("REACT", message);
                            broadCastOther(msg);
                        }
                    }
                    case "QUIT": {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            if (ex instanceof SocketException) {
                System.out.println("Caught socket ex for " + client.getName());
            } else {
                System.out.println(ex);
                ex.printStackTrace();
            }
        } finally {
            try {
                client.getOut().writeObject(new Message("LEFT", ""));;
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (clientList) { //Removes the client from the ArrayList
                clientList.remove(client);
            }
            synchronized (clientHashMap) { //Removes the client from the HashMap
                clientHashMap.remove(client.getUserName());
            }
            System.out.println(client.getName() + " has left.");

            broadcast(new Message("EXIT", client.getUserName())); //Sends EXIT to the client to tell them the user left
            broadcast(new Message("USERS", userString())); //Tells the client to send all userNames in the lobby
        }
    }

    private String userString() { // returns a String of all users
        if (clientList.size() == 0) {
            return "";
        }
        StringBuilder users = new StringBuilder(); //https://docs.oracle.com/javase/7/docs/api/java/lang/StringBuilder.html, easier to use than modifying the user String itself
        users.append("Users: ");
        for (int i = 0; i < clientList.size() - 1; i++) {
            users.append(clientList.get(i).getUserName());
            users.append(", ");
        }
        users.append(clientList.get(clientList.size() - 1).getUserName());
        return users.toString();
    }

    public void reactionAdder (HashMap<String, String> h) {
        h.put("smile", ":)");
        h.put("frown", ":(");
        h.put("wink-smile", ";)");
        h.put("wink-frown", ";(");
        h.put("backwards-smile", "(:");
        h.put("backwards-frown", "):");
        h.put("backwards-wink-smile", "(;");
        h.put("backwards-wink-frown", ");");
        h.put("shrug", "¯\\_(ツ)_/¯");
        h.put("lenny", "( ͡° ͜ʖ ͡°)");
        h.put("flipping-table", "(╯°□°)╯︵ ┻━┻");
        h.put("table-down", "┬─┬ノ( º _ ºノ)");
        h.put("alarmed-table-flip", "(┛◉Д◉)┛彡┻━┻");
        h.put("pointing-at-flipped-table", "(☞ﾟヮﾟ)☞ ┻━┻");
        h.put("flipping-table-look", "(┛ಠ_ಠ)┛彡┻━┻");
        h.put("yeah", "\n( •_•)\n" +
                "( •_•)>⌐■-■\n" +
                "(⌐■_■)");
        h.put("bear", "ʕ•ᴥ•ʔ");
        h.put("cat", "ฅ^•ﻌ•^ฅ");
        h.put("fighting", "(ง •̀_•́)ง");
        h.put("monocle", "ಠ_ರೃ");
        h.put("music", "\n───────────────⚪────\n" +
                "◄◄⠀▐▐ ⠀►►⠀⠀ ⠀ 1:17 / 3:48 ⠀ ───○⠀ ᴴᴰ ⚙ ❐ ⊏⊐");
        h.put("helicopter", "\n▬▬▬.◙.▬▬▬\n" +
                "═▂▄▄▓▄▄▂\n" +
                "◢◤ █▀▀████▄▄▄▄◢◤\n" +
                "█▄ █ █▄ ███▀▀▀▀▀▀▀╬\n" +
                "◥█████◤\n" +
                "══╩══╩═\n" +
                "╬═╬\n" +
                "╬═╬\n" +
                "╬═╬\n" +
                "╬═╬ Hello?\n" +
                "╬═╬☻/\n" +
                "╬═╬/▌\n" +
                "╬═╬/ \\");
        h.put("(/╰^._.^╯\\)", "bat");
        h.put("lenny-shrug", "¯\\_( ͡° ͜ʖ ͡°)_/¯");
        h.put("bird", "ˎ₍•ʚ•₎ˏ");
        h.put("cat-2", "(=^･ｪ･^=)");
        h.put("angry", "ಠ_ಠ");
        h.put("walking", "ᕕ( ᐛ )ᕗ");
        h.put("bunny", "\n[)_(]\n" +
                "(‘ * ’)\n" +
                "(_ _)");
        h.put("creepy", "ಠᴗಠ");
        h.put("bear-2", "(^◕ᴥ◕^)");
        h.put("lenny-money", "[̲̅$̲̅(̲̅ ͡° ͜ʖ ͡°̲̅)̲̅$̲̅]");
        h.put("sad", "ಥ_ಥ");
        h.put("flower-on-face", "(◕‿◕✿)");
        h.put("zombie", "[¬º-°]¬");
    }
}
