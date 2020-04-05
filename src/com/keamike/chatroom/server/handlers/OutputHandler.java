package com.keamike.chatroom.server.handlers;

import com.keamike.chatroom.server.Server;
import com.keamike.chatroom.server.models.User;
import org.json.simple.JSONObject;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OutputHandler {

    private Server server;
    private UserHandler userHandler;
    private PrintWriter out;

    private List<String> commands = new ArrayList<>();

    public OutputHandler(Server server, UserHandler userHandler, PrintWriter out) {
        this.server = server;
        this.userHandler = userHandler;
        this.out = out;
    }

    public void notifyUsers() {
        // Message to be send when user login
        String onlineMsg;
        // List of all connected users (ServerHandlers)
        List<ClientHandler> clientHandlers = server.getClientHandlers();
        // Send users list of other online users
        User thisUser = userHandler.getUserLoggedIn();

        for (ClientHandler handler : clientHandlers) {
            // Check if connected user is logged in
            if (handler.getUserHandler().getUserLoggedIn() != null ) {
                // Then retrieve logged in user
                User otherUser = handler.getUserHandler().getUserLoggedIn();
                // Check if this user is equal to other user
                if (!thisUser.equals(otherUser)) {
                    // Avoid being send a message after logged out
                    if (thisUser.isLoggedIn()) {
                        // Send message to self that other user is online
                        String otherUserState = otherUser.isLoggedIn() ? " is online" : " is offline";
                        onlineMsg = otherUser.getUsername() + otherUserState;
                        // Make it into JSON Object
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("message", onlineMsg);
                        send(jsonObject.toJSONString());
                    }

                    // Send message to other user that you are online
                    String thisUserState = thisUser.isLoggedIn() ? " is online" : " is offline";
                    onlineMsg = thisUser.getUsername() + thisUserState;
                    // Make it into JSON Object
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("message", onlineMsg);
                    handler.getOutputHandler().send(jsonObject.toJSONString());
                }
            }
        }
    }

    public void send(String msg) {
        // Already JSONString
        out.println(msg);
    }

    public void getTime() {
        String time = "Current time is: " + new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
        JSONObject timeObj = new JSONObject();
        timeObj.put("message", time);
        send(timeObj.toJSONString());
    }


    public void getMembersOnline() {
        JSONObject memberObj = new JSONObject();
        if (server.getClientHandlers().size() == 1) {
            String msg = "You are currently the only one online";
            memberObj.put("message", msg);
            send(memberObj.toJSONString());
        } else {
            for (ClientHandler handler : server.getClientHandlers()) {
                if (handler.getUserHandler().getUserLoggedIn() != null) {
                    memberObj.put("message", handler.getUserHandler().getUserLoggedIn().getUsername());
                    send(memberObj.toJSONString());
                }
            }
        }
    }

    public void getAllCommands() {
        if (userHandler.getUserLoggedIn() != null) {
            commands.clear();
            commands.add("logout");
            commands.add("msg <To> : Enter a chat with a user");
            commands.add("online : See who else is online");
            commands.add("time");
            commands.add("help");

        } else {
            commands.clear();
            commands.add("login <Username> <Password>");
            commands.add("time");
            commands.add("quit");
            commands.add("help");
        }

        for (String cmd : commands) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", cmd);
            send(jsonObject.toJSONString());
        }
    }
}
