package com.keamike.chatroom.server.handlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;

public class InputHandler {


    private UserHandler userHandler;
    private SessionHandler sessionHandler;
    private OutputHandler outputHandler;
    private ChatHandler chatHandler;
    private BufferedReader in;

    public InputHandler(UserHandler userHandler, SessionHandler sessionHandler, OutputHandler outputHandler, ChatHandler chatHandler, BufferedReader in) {
        this.userHandler = userHandler;
        this.sessionHandler = sessionHandler;
        this.outputHandler = outputHandler;
        this.chatHandler = chatHandler;
        this.in = in;
    }

    public void cmdSwitch() {
        try {
            String line = "";
            loop:
            while ((line = in.readLine()) != null) {
                JSONObject jsonObj = null;
                // Restructure String to JSON
                JSONParser jsonParser = new JSONParser();
                jsonObj = (JSONObject) jsonParser.parse(line);
                String message = (String) jsonObj.get("message");
                String[] tokens = message.split(" ");
                String cmd = "";
                if (tokens.length > 0 ) {
                    cmd = tokens[0];
                }
                if (tokens.length > 0 && userHandler.getUserLoggedIn() == null) {
                    switch (cmd) {
                        case "login":
                            sessionHandler.login(tokens);
                            break;
                        case "time":
                            outputHandler.getTime();
                            break;
                        case "quit":
                            sessionHandler.quit();
                            break loop;
                        case "help":
                            outputHandler.getAllCommands();
                            break;
                        default:
                            unknownMsg(cmd);
                            break;
                    }
                } else if (tokens.length > 0 && userHandler.getUserLoggedIn() != null) {
                    switch (cmd) {
                        case "logout":
                            sessionHandler.logoff();
                            break;
                        case "online":
                            outputHandler.getMembersOnline();
                            break;
                        case "msg":
                        case "accept":
                            chatHandler.startChat(tokens);
                            break;
                        case "time":
                            outputHandler.getTime();
                            break;
                        case "help":
                            outputHandler.getAllCommands();
                            break;
                        default:
                            unknownMsg(cmd);
                            break;
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void unknownMsg(String cmd) {
        String msg = "unknown " + cmd;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", msg);
        outputHandler.send(jsonObject.toJSONString());
    }
}
