package com.keamike.chatroom.server.handlers;

import com.keamike.chatroom.server.models.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserHandler {

    private User userLoggedIn;

    public List<User> getAllUsers() {

            List<User> users = new ArrayList<>();
            JSONArray jsonArray = getJsonArray();
            if (jsonArray != null) {
                for (Object o : jsonArray) {
                    JSONObject user = (JSONObject) o;
                    users.add(parseUserObject((JSONObject) user.get("user")));
                }
                return users;
            }
            return null;
    }

    private JSONArray getJsonArray() {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("database.json")) {
            // Read JSON file
            return (JSONArray) jsonParser.parse(reader);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User parseUserObject(JSONObject obj) {
        User user = new User();
        user.setUsername((String) obj.get("username"));
        user.setPassword((String ) obj.get("password"));
        user.setLoggedIn((boolean) obj.get("isLoggedIn"));
        return user;
    }

    public void updateUser() {
        JSONArray jsonArray = getJsonArray();
        try (FileWriter writer = new FileWriter("database.json")) {
            if (jsonArray != null) {
                for (Object o : jsonArray) {
                    // Cast to JSONObject
                    JSONObject jsonObject = (JSONObject) o;
                    // Get user from JSONObject
                    JSONObject jsonUser = (JSONObject) jsonObject.get("user");
                    // Check if JSONObject user matches userLoggedIn
                    if (jsonUser.get("username").equals(userLoggedIn.getUsername()) && jsonUser.get("password").equals(userLoggedIn.getPassword())) {
                        // If so, put new data into JSONObject user
                        jsonUser.put("username", userLoggedIn.getUsername());
                        jsonUser.put("password", userLoggedIn.getPassword());
                        jsonUser.put("isLoggedIn", userLoggedIn.isLoggedIn());
                    }
                }
            }
            if (jsonArray != null) {
                writer.write(jsonArray.toJSONString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } ;
    }

    public User getUserLoggedIn() {
        return userLoggedIn;
    }

    public void setUserLoggedIn(User user) {
        userLoggedIn = user;
    }

    public boolean handleLogin(PrintWriter out, String[] tokens, boolean state) {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            List<User> users = getAllUsers();
            for(User user : users) {
                if (login.equals(user.getUsername()) && password.equals(user.getPassword())) {
                    String msg = "You have now been logged in: " + login;
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("message", msg);
                    user.setLoggedIn(state);
                    setUserLoggedIn(user);
                    updateUser();
                    out.println(jsonObject.toJSONString());
                    return true;
                }
            }
        }
        return false;
    }
}
