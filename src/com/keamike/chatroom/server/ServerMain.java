package com.keamike.chatroom.server;

public class ServerMain {
    public static void main(String[] args) {
        final int PORT = 1234;
        Server server = new Server(PORT);
        server.start();
    }
}
