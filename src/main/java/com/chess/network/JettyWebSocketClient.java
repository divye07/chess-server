package com.chess.network;

import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JettyWebSocketClient {
    public static void main(String[] args) throws Exception {
        String destUri = "wss://chess-server-3k9p.onrender.com/ws";
        WebSocketClient client = new WebSocketClient();
        SimpleSocket socket = new SimpleSocket();

        try {
            client.start();
            URI echoUri = new URI(destUri);
            client.connect(socket, echoUri).get();
            socket.awaitClose(5, TimeUnit.SECONDS);
        } finally {
            client.stop();
        }
    }

    @WebSocket
    public static class SimpleSocket {
        private final CountDownLatch closeLatch = new CountDownLatch(1);

        public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
            return this.closeLatch.await(duration, unit);
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            System.out.println("Connected to server");
            try {
                session.getRemote().sendString("Hello from Java client!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnWebSocketMessage
        public void onMessage(String msg) {
            System.out.println("Received: " + msg);
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            System.out.println("Connection closed: " + reason);
            closeLatch.countDown();
        }
    }
} 