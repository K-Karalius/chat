package com.karalius.server;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {

    @FXML
    private Button bttn_startServer;
    @FXML
    private Button bttn_stopServer;
    @FXML
    private Button bttn_addRoom;
    @FXML
    private Button bttn_deleteRoom;
    @FXML
    private ComboBox<String> cb_clients;
    @FXML
    private ComboBox<String> cb_rooms;
    @FXML
    private TextField tf_name;
    private Thread thread;
    private Server server;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bttn_startServer.setDisable(false);
        bttn_stopServer.setDisable(true);
        setComponents(true);
    }

    @FXML
    void startServer(){

        server = new Server();
        thread = new Thread(server);
        thread.start();

        bttn_startServer.setDisable(true);
        bttn_stopServer.setDisable(false);
        setComponents(false);

    }

    @FXML
    void stopServer(){
        server.shutdown();
        cb_rooms.getItems().clear();

        bttn_startServer.setDisable(false);
        bttn_stopServer.setDisable(true);
        setComponents(true);
    }
    @FXML
    void addRoom() {
        String roomName = tf_name.getText().trim();
        if(!roomName.isEmpty()){
            if(!cb_rooms.getItems().contains(roomName)){
                cb_rooms.getItems().add(roomName);
                server.addRoom(roomName);
                tf_name.clear();
            }
        }
    }

    @FXML
    void deleteRoom() {
        String roomName = cb_rooms.getValue();
        if(roomName != null){
            cb_rooms.getItems().remove(roomName);
            server.removeRoom(roomName);
        }
    }

    void setComponents(boolean set){
        bttn_addRoom.setDisable(set);
        bttn_deleteRoom.setDisable(set);
        cb_rooms.setDisable(set);
        cb_clients.setDisable(set);
        tf_name.setDisable(set);
    }
}