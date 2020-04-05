package com.keamike.chatroom.server;

import com.keamike.chatroom.server.handlers.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
    private final int port;
    private List<ClientHandler> clientHandlers = new ArrayList<>();

    public Server(int port) {
        this.port = port;
    }

    public List<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    @Override
    public void run() {
        try {
            System.out.println("Waiting for client connection...");
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {
                // Only continues when client has connected
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, socket);
                clientHandler.start();
                clientHandlers.add(clientHandler);
                System.out.println("Client has been connected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
