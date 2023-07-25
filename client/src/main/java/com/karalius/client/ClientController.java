package com.karalius.client;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class ClientController {

    @FXML Button bttn_join;
    @FXML
    private TextField tf_message;
    @FXML
    private TextArea ta_chatBox;
    private Client client;
    private Thread clientThread;

    @FXML
    void joinServer(){
        client = new Client(ta_chatBox);
        clientThread = new Thread(client);
        clientThread.start();
    }

    @FXML
    void send(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER){
            String message = tf_message.getText().trim();
            if(!message.isEmpty()){
                client.sendMessage(message);
                tf_message.clear();
            }
        }
    }
}