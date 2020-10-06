
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ChatServer {
    public static final int PORT = 54321;
    private static final ArrayList<ClientConnectionData> clientList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(100);

        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Chat Server started.");
            System.out.println("Local IP: "
                    + Inet4Address.getLocalHost().getHostAddress());
            System.out.println("Local Port: " + serverSocket.getLocalPort());
        
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.printf("Connected to %s:%d on local port %d\n",
                        socket.getInetAddress(), socket.getPort(), socket.getLocalPort());
                    
                    // This code should really be done in the separate thread
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    String name = socket.getInetAddress().getHostName();

                    ClientConnectionData client = new ClientConnectionData(socket, in, out, name);
                    synchronized (clientList) {
                        clientList.add(client);
                    }
                    
                    System.out.println(name + " has joined");

                    //handle client business in another thread
                    pool.execute(new ClientHandler(client));
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }

            }
        } 
    }

    // Inner class 
    static class ClientHandler implements Runnable {
        // Maintain data about the client serviced by this thread
        ClientConnectionData client;

        public ClientHandler(ClientConnectionData client) {
            this.client = client;
        }

        HashMap reactionHash = new HashMap<String, String>();

        /**
		 * Broadcasts a message to all clients connected to the server.
		 */
        public void broadcast(String msg) {
            try {
                System.out.println("Broadcasting -- " + msg);
                synchronized (clientList) {
                    for (ClientConnectionData c : clientList){
                        c.getOut().println(msg);
                        // c.getOut().flush();
                    }
                }
            } catch (Exception ex) {
                System.out.println("broadcast caught exception: " + ex);
                ex.printStackTrace();
            }
            
        }

        public void broadcastChat(String msg, String name) {
            try {
                ArrayList<ClientConnectionData> newClientList = clientList;
                newClientList.remove(client.getUserName());
                System.out.println("Broadcasting -- " + msg);
                synchronized (newClientList) {
                    for (ClientConnectionData c : newClientList){
                        c.getOut().println(msg);
                        // c.getOut().flush();
                    }
                }
            } catch (Exception ex) {
                System.out.println("broadcast caught exception: " + ex);
                ex.printStackTrace();
            }

        }

        public void broadcastPChat(String msg, String name) {
            try {
                ArrayList<ClientConnectionData> newClientList = new ArrayList<>();
                for (int i = 0; i < clientList.size(); i++) {
                    if (clientList.get(i).getUserName() == name) {
                        newClientList.add(clientList.get(i));
                    }
                }
                System.out.println("Broadcasting -- " + msg);
                synchronized (newClientList) {
                    for (ClientConnectionData c : newClientList){
                        c.getOut().println(msg);
                        // c.getOut().flush();
                    }
                }
            } catch (Exception ex) {
                System.out.println("broadcast caught exception: " + ex);
                ex.printStackTrace();
            }

        }

        @Override
        public void run() {
            try {
                BufferedReader in = client.getInput();
                //get userName, first message from user
                String userName = in.readLine().trim();
                client.setUserName(userName);
                //notify all that client has joined
                broadcast(String.format("WELCOME %s", client.getUserName()));

                reactionAdder(reactionHash);
                
                String incoming = "";

                while( (incoming = in.readLine()) != null) {
                    if (incoming.startsWith("CHAT")) {
                        String chat = incoming.substring(4).trim();
                        if (chat.length() > 0) {
                            String msg = String.format("%s: %s", client.getUserName(), chat);
                            broadcastChat(msg, client.getUserName());
                        }
                    } else if (incoming.startsWith("QUIT")){
                        break;
                    } else if (incoming.startsWith("PCHAT")) {
                        String nameAndChat = incoming.substring(5).trim();
                        String name = "";
                        int i = 0;
                        while (!(nameAndChat.charAt(i) == (' '))) {
                            name += nameAndChat.charAt(i);
                        }
                        String chat = nameAndChat.substring(name.length() + 1);
                        if (chat.length() > 0) {
                            String msg = String.format("%s (private): %s", client.getUserName(), chat);
                            broadcastPChat(msg, client.getUserName());
                        }
                    } else if (incoming.startsWith("REACT")) {
                        String key = incoming.substring(5).trim();
                        String reaction = (String) reactionHash.get(key);
                        String msg = String.format("%s Reacted: %s", client.getUserName(), reaction);
                        broadcastChat(msg, client.getUserName());
                    }
                }
            } catch (Exception ex) {
                if (ex instanceof SocketException) {
                    System.out.println("Caught socket ex for " + 
                        client.getName());
                } else {
                    System.out.println(ex);
                    ex.printStackTrace();
                }
            } finally {
                //Remove client from clientList, notify all
                synchronized (clientList) {
                    clientList.remove(client); 
                }
                System.out.println(client.getName() + " has left.");
                broadcast(String.format("EXIT %s", client.getUserName()));
                try {
                    client.getSocket().close();
                } catch (IOException ex) {}

            }
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
            h.put("yeah", "( •_•)\n" +
                    "( •_•)>⌐■-■\n" +
                    "(⌐■_■)");
            h.put("bear", "ʕ•ᴥ•ʔ");
            h.put("cat", "ฅ^•ﻌ•^ฅ");
            h.put("fighting", "(ง •̀_•́)ง");
            h.put("monocle", "ಠ_ರೃ");
            h.put("music", "───────────────⚪────\n" +
                    "◄◄⠀▐▐ ⠀►►⠀⠀ ⠀ 1:17 / 3:48 ⠀ ───○⠀ ᴴᴰ ⚙ ❐ ⊏⊐");
            h.put("helicopter", "▬▬▬.◙.▬▬▬\n" +
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
            h.put("bunny", "[)_(]\n" +
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

}
