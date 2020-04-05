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

    private boolean isChatting = false;

    public ChatHandler(Server server, UserHandler userHandler, PrintWriter out, BufferedReader in) {
        this.server = server;
        this.userHandler = userHandler;
        this.out = out;
        this.in = in;
    }

    public void startChat(String[] tokens) {
        // Chat can only be initiated when logged in, therefore assign user
        user = userHandler.getUserLoggedIn();

        if (tokens.length <= 1) {
            String errMsg = "Invalid command... 'msg <To>, accept <Recipient> or decline <Recipient>";
            sendToSender(errMsg);
        } else {
            String recipientName = tokens[1];
            if (recipientName.equals(user.getUsername())) {
                String errMsg = "You can't chat with yourself...";
                sendToSender(errMsg);
            } else {
                // Find recipient sender was querying
                for (ClientHandler handler : server.getClientHandlers()) {
                    if (handler.getUserHandler().getUserLoggedIn().getUsername().equals(recipientName)) {
                        recipientClient = handler;
                        recipient = recipientClient.getUserHandler().getUserLoggedIn();
                    }
                }
            }

            if (recipientClient == null && !recipientName.equals(user.getUsername())) {
                String errMsg = "The user you wanted to chat with is currently not online";
                JSONObject errObj = new JSONObject();
                errObj.put("message", errMsg);
                out.println(errObj.toJSONString());
            } else if (tokens[0].equals("accept") && recipient.isChatting()) {
                // Messages to be send to both sender and recipient
                String msgToSender = "You accepted " + recipient.getUsername() + "'s chat request\nStart Chatting:";
                String msgToRecipient = user.getUsername() + " accepted your chat request!\nStart Chatting:";

                // Send message to sender
                sendToSender(msgToSender);

                // Send message to recipient
                JSONObject recipientObj = new JSONObject();
                recipientObj.put("message", msgToRecipient);
                recipientClient.getOutputHandler().send(recipientObj.toJSONString());

                // The user which accept, should be set to isChatting and join the chat
                user.setChatting(true);
                isChatting = true;
                joinChat();

            } else if (tokens[0].equals("decline") && recipient.isChatting()) {
                // Messages to be send to sender and recipient
                String senderMsg = "You declined " + recipient.getUsername() + "'s chat request";
                String recipientMsg = user.getUsername() + " declined your chat request";

                // Send message to sender
                sendToSender(senderMsg);

                // Send message to recipient
               sendToRecipient(recipientMsg);

            } else if (tokens[0].equals("msg")) {
                // Messages to be send to sender and recipient
                String senderMsg = "You are now in a chat with " + recipient.getUsername() + "\nBut before you can start chatting " + recipient.getUsername() + " has to accept your request";
                String recipientMsg = user.getUsername() + " want to chat\nWrite: 'accept " + user.getUsername() + "' to start messaging with " + user.getUsername() +
                        " or 'decline " + user.getUsername() + "'' to reject";

                // Send message to sender
                sendToSender(senderMsg);

                // Send message to recipient
                sendToRecipient(recipientMsg);

                user.setChatting(true);
                isChatting = true;
                joinChat();
            } else {
                String errMsg = "You have not been requested to chat... write msg <To> to start a chat";
                sendToSender(errMsg);
            }
        }
    }

    public void joinChat() {
        while (isChatting) {
            try {
                JSONParser parser = new JSONParser();

                // Message received from user
                JSONObject msgObj = (JSONObject) parser.parse(in.readLine());
                String msg = (String) msgObj.get("message");

                // If one of the users leave the chat
                if (user.isChatting() && msg.length() == 4 && msg.equalsIgnoreCase("quit")) {
                    // Send info message to both chat users
                    String senderMsg = "You have left the chat";
                    sendToSender(senderMsg);

                    String recipientMsg = user.getUsername() + " left the chat";
                    sendToRecipient(recipientMsg);

                    // Set both users isChatting to false and close chat while loop
                    user.setChatting(false);
                    recipient.setChatting(false);

                    isChatting = false;
                    recipientClient.getChatHandler().setChatting(false);

                // If both users isChatting they will receive each others messages
                } else if (user.isChatting() && recipient.isChatting()) {
                    // Refactor message
                    String fromMsg = userHandler.getUserLoggedIn().getUsername() + ": " + msg;
                    // Send recipient message from other user
                    sendToRecipient(fromMsg);
                } else {
                    String errMsg = "You are alone in the chatroom, wait for user to join or leave\n";
                    sendToSender(errMsg);
                }
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setChatting(boolean chatting) {
        isChatting = chatting;
    }

    private void sendToSender(String msg) {
        JSONObject senderObj = new JSONObject();
        senderObj.put("message", msg);
        out.println(senderObj.toJSONString());
    }

    private void sendToRecipient(String msg) {
        JSONObject recipientObj = new JSONObject();
        recipientObj.put("message", msg);
        recipientClient.getOutputHandler().send(recipientObj.toJSONString());
    }
}
