package application.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Data {

    @JsonProperty("clients")
    private ArrayList<Server.ConnectionHandler> clients;

    @JsonProperty("rooms")
    private ArrayList<Room> rooms;


    public void setNewClientList(ArrayList<Server.ConnectionHandler> arrayList){
        this.clients = arrayList;
    }

    public void setNewRoomList(ArrayList<Room> arrayList){
        this.rooms = arrayList;
    }

    public ArrayList<Server.ConnectionHandler> getClients(){
        return this.clients;
    }

    public ArrayList<Room> getRooms(){
        return this.rooms;
    }

}

