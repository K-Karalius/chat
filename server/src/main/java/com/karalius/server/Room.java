package com.karalius.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.ArrayList;

public class Room {
    @JsonProperty("roomName")
    private String roomName;
    private ArrayList<Server.ConnectionHandler> connections;
    @JsonProperty()
    private ArrayList<String> messageList;


    public String getRoomName(){
        return roomName;
    }

    public ArrayList<String> getMessageList(){
        return messageList;
    }
    public Room(String name){
        this.roomName = name;
        connections = new ArrayList<>();
        messageList = new ArrayList<>();
    }

    public void addConnection(Server.ConnectionHandler connection){
        connections.add(connection);
    }

    public void removeConnection(Server.ConnectionHandler connection){
        connections.remove(connection);
    }

    public void writeToAll(String message) throws IOException {
        for(Server.ConnectionHandler ch : connections){
            ch.sendMessage(message);
        }
        messageList.add(message);
    }


}
