package com.chess.network;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;

public class JettyWebSocketServer {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "5000"));
        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
            wsContainer.addMapping("/ws", (req, resp) -> new ChessWebSocket());
        });

        server.start();
        System.out.println("Jetty WebSocket server started on port " + port);
        server.join();
    }

    @WebSocket
    public static class ChessWebSocket {
        @OnWebSocketConnect
        public void onConnect(Session session) throws IOException {
            System.out.println("WebSocket connected: " + session.getRemoteAddress());
            session.getRemote().sendString("Welcome to the Jetty Chess WebSocket Server!");
        }

        @OnWebSocketMessage
        public void onMessage(Session session, String message) throws IOException {
            System.out.println("Received: " + message);
            session.getRemote().sendString("Echo: " + message);
        }

        @OnWebSocketClose
        public void onClose(Session session, int statusCode, String reason) {
            System.out.println("WebSocket closed: " + reason);
        }

        @OnWebSocketError
        public void onError(Session session, Throwable error) {
            System.err.println("WebSocket error: " + error.getMessage());
        }
    }
} 