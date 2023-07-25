package com.karalius.server;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private  ServerSocket serverSocket;
    private ArrayList<ConnectionHandler> connections;
    private ArrayList<Room> rooms;
    private boolean done;
    private ExecutorService pool;

    public Server(){
        this.connections = new ArrayList<>();
        this.rooms = new ArrayList<>();
        done = false;
        pool = Executors.newCachedThreadPool();
    }
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(9999);
            while(!done){
                Socket clientSocket = serverSocket.accept();
                ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket);
                connections.add(connectionHandler);
                pool.execute(connectionHandler);
            }
        }catch (IOException e){
            shutdown();
        }
    }

    public void addRoom(String name){
        Room room = new Room(name);
        rooms.add(room);
    }
    public void removeRoom(String name){
        for(Room tempRoom : rooms){
            if(tempRoom.getRoomName().equals(name)){
                rooms.remove(tempRoom);
                break;
            }
        }
    }
    public Room getRoom(String name){
        for(Room tempRoom : rooms){
            if(tempRoom.getRoomName().equals(name)){
                return tempRoom;
            }
        }
        return null;
    }

    public boolean IsNickNameAvailable(String newNickname){
        for(ConnectionHandler ch : connections){
            if(ch.nickname != null && ch.nickname.equals(newNickname)){
                return false;
            }
        }
        return true;
    }

    public ConnectionHandler getConnectionHandler(String nick){
        for(ConnectionHandler ch : connections){
            if(nick.equals(ch.nickname)){
                return ch;
            }
        }
        return null;
    }

    public void saveDataToFile(){
        Data data = new Data();
        data.setNewClientList(connections);
        data.setNewRoomList(rooms);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File outputFile = new File("data.json");
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(outputFile, data);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void shutdown(){
        try{
            done = true;
            pool.shutdown();

            if(!serverSocket.isClosed()){
                serverSocket.close();
            }
            for(ConnectionHandler ch : connections){
                ch.shutdown();
            }
            saveDataToFile();

        }catch(Exception e){
            //nothing to do
        }
    }
    public class ConnectionHandler implements Runnable {


        private Socket client;
        @JsonProperty("nickname")
        private String nickname;
        private BufferedReader in;
        private PrintWriter out;
        private Room currentRoom;
        @JsonProperty("directMessageRooms")
        private ArrayList<DirectMessageRoom> directMessageRooms;
        private DirectMessageRoom currentDMRoom;
        private String privateOrGroup;


        // for json reading
        public String getNickname() {
            return nickname;
        }

        // for json reading
        public ArrayList<DirectMessageRoom> getDirectMessageRooms() {
            return directMessageRooms;
        }


        public ConnectionHandler(Socket socket){
            this.client = socket;
            this.directMessageRooms = new ArrayList<>();
            privateOrGroup = "Neither";
        }

        @Override
        public void run() {
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                setClientNickName();
                String message;
                while((message = in.readLine()) != null){
                    if(message.startsWith("/join ")){
                        String roomName = message.substring(6);
                        changeRoom(roomName);
                    } else if(message.startsWith("/disconnect")){
                        if(currentRoom != null && privateOrGroup.equals("Group")){
                            currentRoom.writeToAll(nickname + " disconnected!");
                        }
                        sendMessage("CLEAR");
                        sendMessage("Server: You disconnected from server!");
                        shutdown();
                    }else if(message.startsWith("/printroomlist")){
                        printRooms();
                    }else if(message.startsWith("/printmembers")){
                        printClients();
                    }else if(message.startsWith("/msg ")){
                        String DMNickname = message.substring(5);
                        changeDirectMessageRoom(DMNickname);
                    }else if(privateOrGroup.equals("GROUP")){
                        currentRoom.writeToAll(nickname + ": " + message);
                    }else if(privateOrGroup.equals("PRIVATE")){
                        currentDMRoom.broadcast(nickname + ": " + message);
                    }
                }
            }catch (IOException e){
                shutdown();
            }
        }

        public void printRooms(){
            sendMessage("Server: Rooms->");
            for(Room room : rooms){
                sendMessage(room.getRoomName());
            }
        }
        public void printClients(){
            sendMessage("Server: CLIENTS->");
            for(ConnectionHandler ch : connections){
                if(!this.nickname.equals(ch.nickname)){
                    sendMessage(ch.nickname);
                }
            }
        }

        public void changeRoom(String roomName) throws IOException{
            if(currentRoom != null){
                if(currentRoom.getRoomName().equals(roomName) && privateOrGroup.equals("GROUP")){
                    sendMessage("Server: You are already in this room");
                    return;
                }
            }
            Room room = getRoom(roomName);
            if(room != null){
                if(currentRoom != null) {
                    currentRoom.writeToAll(nickname + " left the chat!");
                    currentRoom.removeConnection(this);
                }

                room.writeToAll(nickname + " joined the room!");
                sendMessage("CLEAR");
                ArrayList<String> messages = room.getMessageList();
                sendMultiLinedString(messages);

                room.addConnection(this);
                currentRoom = room;
                privateOrGroup = "GROUP";
            }else{
                sendMessage("Server: Room doesn't exist!");
            }
        }

        public void changeDirectMessageRoom(String DMNickname){
            if(DMNickname.equals(nickname)){
                sendMessage("Server: Can not message yourself...");
                return;
            }else if(getConnectionHandler(DMNickname) == null){
                sendMessage("Server: User does not exist!");
                return;
            }

            DirectMessageRoom room = getDirectRoom(DMNickname);
            if(room == null){
                ConnectionHandler otherClient = getConnectionHandler(DMNickname);
                room = new DirectMessageRoom();
                room.addClient(nickname, this);
                room.addClient(DMNickname, otherClient);
                directMessageRooms.add(room);
                otherClient.directMessageRooms.add(room);
            }
            sendMessage("CLEAR");
            ArrayList<String> messages = room.getMessages();
            sendMultiLinedString(messages);

            currentDMRoom = room;
            currentDMRoom.setClientConnection(nickname);
            privateOrGroup = "PRIVATE";
        }


        public DirectMessageRoom getDirectRoom(String DMNickname){
            for(DirectMessageRoom room : directMessageRooms){
                if(room.IsClient(DMNickname) != null){
                    return room;
                }
            }
            return null;
        }

        public void setClientNickName() throws IOException{
            String tempNickname;
            sendMessage("Server: Please enter a nickname");
            while(true){
                tempNickname = in.readLine();
                if(IsNickNameAvailable(tempNickname)){
                    break;
                }
                sendMessage("Server: Nickname already exists!");
            }
            nickname = tempNickname;
            sendMessage("Server: You nickname is: " + nickname);
        }

        public void sendMultiLinedString(ArrayList<String> messages){
            for(String line : messages){
                sendMessage(line);
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }
        public void shutdown(){
            try {
                sendMessage("/disconnect");
                if(!client.isClosed()){
                    client.close();
                }
            }catch(Exception e){
                // nothing to do
            }
        }
    }
}
