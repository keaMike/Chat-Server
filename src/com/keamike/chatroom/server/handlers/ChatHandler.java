package com.keamike.chatroom.server.handlers;

import com.keamike.chatroom.server.Server;
import com.keamike.chatroom.server.models.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ChatHandler {

    private Server server;
    private UserHandler userHandler;
    private PrintWriter out;
    private BufferedReader in;
    private ClientHandler recipientClient = null;
    private User recipient = null;
    private User user = null;

    public ChatHandler(Server server, UserHandler userHandler, PrintWriter out, BufferedReader in) {
        this.server = server;
        this.userHandler = userHandler;
        this.out = out;
        this.in = in;
    }

    public void startChat(String[] tokens) {
        String recipientName = tokens[1];
        // Find recipient sender was querying
        for (ClientHandler handler : server.getClientHandlers()) {
            if (handler.getUserHandler().getUserLoggedIn().getUsername().equals(recipientName)) {
                recipientClient = handler;
            }
        }

        if (recipient == null) {
            String errMsg = "The user you wanted to chat with is currently not online";
            JSONObject errObj = new JSONObject();
            errObj.put("message", errMsg);
            send(errObj.toJSONString());
        } else {
            user.setChatting(true);
            String senderInfo = "You are now in a chat with " + recipientName + "\nStart messaging!";
            String recipientInfo = user.getUsername() + " want to chat\nWrite: 'accept " + user.getUsername() + "' to start messaging with " + user.getUsername() +
                    " or 'decline " + recipientName + "'' to reject";

            JSONObject recipientInfoObj = new JSONObject();
            recipientInfoObj.put("message", recipientInfo);
            recipientClient.getOutputHandler().send(recipientInfoObj.toJSONString());

            JSONObject senderInfoObj = new JSONObject();
            senderInfoObj.put("message", senderInfo);
            send(senderInfoObj.toJSONString());
        }
    }

    public void joinChat() {
        while (true) {
            try {
                JSONParser parser = new JSONParser();

                // Message received from sender
                JSONObject senderObj = (JSONObject) parser.parse(in.readLine());
                String senderInitMsg = (String) senderObj.get("message");

                if (!user.isChatting() && senderInitMsg.length() == 7 && senderInitMsg.equals("decline")) {
                    break;
                }
                if (!user.isChatting() && senderInitMsg.length() == 6 && senderInitMsg.equals("accept")) {
                    String acceptanceMsg = recipient.getUsername() + " has accepted your chat request";
                    JSONObject acceptanceObj = new JSONObject();
                    acceptanceObj.put("message", acceptanceMsg);
                    recipientClient.getOutputHandler().send(acceptanceObj.toJSONString());
                    recipientClient.getUserHandler().getUserLoggedIn().setChatting(true);
                    continue;
                }
                if (user.isChatting() && senderInitMsg.length() == 4 && senderInitMsg.equalsIgnoreCase("quit")) {
                    // Send error message to both chat users
                    String errMsg = "You or the recipient quited your chat";
                    senderObj.put("message", errMsg);
                    send(senderObj.toJSONString());
                    recipientClient.getOutputHandler().send(senderObj.toJSONString());
                    break;
                }
                if (user.isChatting()) {
                    // Refactor message
                    String senderMsg = userHandler.getUserLoggedIn().getUsername() + ": " + senderInitMsg;
                    // Re initiate json object
                    senderObj = new JSONObject();
                    senderObj.put("message", senderMsg);
                    recipientClient.getOutputHandler().send(senderObj.toJSONString());
                }
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    }

    private void send(String msg) {
        // Already JSONString
        out.println(msg);
    }
}
