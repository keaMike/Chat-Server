package com.keamike.chatroom.server.handlers;

import com.keamike.chatroom.server.models.User;
import com.keamike.chatroom.server.Server;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket clientSocket;
    private Server server;
    private UserHandler userHandler;

    private PrintWriter out;
    private BufferedReader in;
    private SessionHandler sessionHandler;
    private ChatHandler chatHandler;
    private InputHandler inputHandler;
    private OutputHandler outputHandler;

    public ClientHandler(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
        initializeHandlers();
    }

    @Override
    public void run() {
        handleClientSocket();
    }

    public void initializeHandlers() {
        try {
            userHandler = new UserHandler();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            chatHandler = new ChatHandler(server, userHandler, out, in);
            outputHandler = new OutputHandler(server, userHandler, out);
            sessionHandler = new SessionHandler(server, userHandler, outputHandler, out);
            inputHandler = new InputHandler(userHandler, sessionHandler, outputHandler, chatHandler, in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleClientSocket() {
        try {
            // Send welcome message
            String welcomeMsg = "Welcome to WeChat";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", welcomeMsg);
            out.println(jsonObject.toJSONString());
            // Start Command Switch
            inputHandler.cmdSwitch();
            // Close connection after ending Switch
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UserHandler getUserHandler() {
        return userHandler;
    }

    public SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    public ChatHandler getChatHandler() {
        return chatHandler;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public OutputHandler getOutputHandler() {
        return outputHandler;
    }
}
