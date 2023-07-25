package com.karalius.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Hashtable;

public class DirectMessageRoom {

    private Hashtable<String, Server.ConnectionHandler> clients; // max 2
    private Hashtable<String, String> connectedClients;  // max 2, maybe I should add the limiter, but this should be enough
                                                        //key value is name, other is "connected" or "disconnected"
    @JsonProperty("chatNicknames")
    private ArrayList<String> chatNicknames;            // for json
    @JsonProperty("messages")
    private ArrayList<String> messages;



    public ArrayList<String> getMessages(){
        return messages;
    }


    // for json reading
    public ArrayList<String> getChatNicknames() {
        return chatNicknames;
    }


    public DirectMessageRoom(){
        clients = new Hashtable<>();
        connectedClients = new Hashtable<>();
        messages = new ArrayList<>();
        chatNicknames = new ArrayList<>(2);
    }

    public void addClient(String name, Server.ConnectionHandler client){
         clients.put(name, client);
         connectedClients.put(name, "disconnected");
         chatNicknames.add(name);
    }

    public void setClientConnection(String nickname){
        connectedClients.put(nickname, "connected");
    }

    public void broadcast(String message){
        String connectivity;
        Server.ConnectionHandler client;

        for(String key : connectedClients.keySet()){
            connectivity = connectedClients.get(key);
            if(connectivity.equals("connected")){
                client = clients.get(key);
                client.sendMessage(message);
            }
        }
        messages.add(message);
    }

    public Server.ConnectionHandler IsClient(String nickname){
        return clients.get(nickname);
    }


}
