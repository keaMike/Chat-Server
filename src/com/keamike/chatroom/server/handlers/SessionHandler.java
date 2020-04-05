package com.keamike.chatroom.server.handlers;

import com.keamike.chatroom.server.Server;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class SessionHandler {

    private Server server;
    private UserHandler userHandler;
    private OutputHandler outHandler;
    private PrintWriter out;

    public SessionHandler(Server server, UserHandler userHandler, OutputHandler outputHandler, PrintWriter out) {
        this.server = server;
        this.userHandler = userHandler;
        this.outHandler = outputHandler;
        this.out = out;
    }

    public void login(String[] tokens) {
        // Check if user was logged in
        if (userHandler.handleLogin(out, tokens, true)) {
            System.out.println("Client " + userHandler.getUserLoggedIn().getUsername() + " has logged in");
            // If so, notify other users
            if (server.getClientHandlers().size() > 1) {
                outHandler.notifyUsers();
            }
        } else {
            String msg = "Failed to login, please try again";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", msg);
            outHandler.send(jsonObject.toJSONString());
        }
    }

    public void logoff()  {
        userHandler.getUserLoggedIn().setLoggedIn(false);
        userHandler.updateUser();
        outHandler.notifyUsers();
        String logoffMsg = userHandler.getUserLoggedIn().getUsername() + " you have been logged off";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", logoffMsg);
        outHandler.send(jsonObject.toJSONString());
        System.out.println("Client " + userHandler.getUserLoggedIn().getUsername()+ " has logged off");
        userHandler.setUserLoggedIn(null);
    }


    public void quit()  {
        String disconnectedMsg = "You have been disconnected";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", disconnectedMsg);
        outHandler.send(jsonObject.toJSONString());
        System.out.println("Client has been disconnected");
        server.getClientHandlers().remove(this);
    }
}
