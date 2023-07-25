package com.karalius.client;

import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private final TextArea  chatBox;
    public Client(TextArea chatBox){
        this.chatBox = chatBox;
    }

    @Override
    public void run() {
        try{
            clientSocket = new Socket("localhost", 9999);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


            String message;
            while((message = in.readLine()) != null){
                if (message.equals("CLEAR")) {
                    chatBox.clear();
                }else if(message.startsWith("/disconnect")){
                    shutdown();
                }else{
                    chatBox.appendText(message + "\n");
                }
            }
        }catch(IOException e){
            shutdown();
        }
    }

    public void shutdown(){
        try{
            if(!clientSocket.isClosed()){
                clientSocket.close();
            }
        }catch (IOException e){
            //nothing to do
        }
    }

    public void sendMessage(String message){
        if(message.startsWith("/disconnect")){
            out.println("/disconnect");
            shutdown();
        }else{
            out.println(message);
        }

    }

}
