package nl.jessevogel.photomanager.httpserver;

import nl.jessevogel.photomanager.MIMEType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public abstract class HTTPServer {

    private static final int BACKLOG = 10;

    private int port;
    private ServerSocket serverSocket;
    private boolean acceptingClients;

    public HTTPServer(int port) {
        this.port = port;
    }

    public boolean start() {
        try {
            // Setup server socket and start server thread
            serverSocket = new ServerSocket(port, BACKLOG);
            acceptingClients = true;
            HTTPServerThread thread = new HTTPServerThread();
            thread.start();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to start HTTP server");
            return false;
        }
    }

    public boolean stop() {
        try {
            acceptingClients = false;
            serverSocket.close();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to stop HTTP server");
            return false;
        }
    }

    public abstract boolean respond(HTTPRequest request, HTTPResponse response);

    public int getPort() {
        return port;
    }

    private class HTTPServerThread extends Thread {
        public void run() {
            try {
                while (acceptingClients) {
                    // Accept clients and for each start a client thread
                    Socket clientSocket = serverSocket.accept();
                    if (clientSocket == null) break;
                    HTTPServerClientThread clientThread = new HTTPServerClientThread(clientSocket);
                    clientThread.start();
                }
            } catch (SocketException e) {
                // The socket closed, this is totally fine!
            } catch (IOException e) {
                System.err.println("Failed to accept client");
            }
        }
    }

    private class HTTPServerClientThread extends Thread {

        private Socket clientSocket;

        HTTPServerClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                HTTPRequest request = new HTTPRequest();
                HTTPResponse response = new HTTPResponse();

                // Try to parse request and construct response
                if (!request.parse(clientSocket.getInputStream()) || !respond(request, response))
                    errorBadRequest(request, response);

                // Send response and close socket
                if (!response.send(clientSocket.getOutputStream()))
                    System.err.println("Unable to send response to client");
                clientSocket.close();

            } catch (IOException e) {
                System.err.println("Failed to handle client");
            }
        }

    }

    public static boolean errorBadRequest(HTTPRequest request, HTTPResponse response) {
        response.setStatusLine("HTTP/1.1 400 Bad Request");
        response.setHeader("Content-Type", "text/plain");
        response.addMessage("Bad Request");
        return true;
    }

    public static boolean errorForbidden(HTTPRequest request, HTTPResponse response) {
        response.setStatusLine("HTTP/1.1 403 Forbidden");
        response.setHeader("Content-Type", "text/plain");
        response.addMessage("Forbidden");
        return true;
    }

    public static boolean errorNotFound(HTTPRequest request, HTTPResponse response) {
        response.setStatusLine("HTTP/1.1 404 Not Found");
        response.setHeader("Content-Type", "text/plain");
        response.addMessage("Not Found");
        return true;
    }

    public static boolean errorInternalServerError(HTTPRequest request, HTTPResponse response) {
        response.setStatusLine("HTTP/1.1 500 Internal Server Error");
        response.setHeader("Content-Type", "text/plain");
        response.addMessage("Internal Server Error");
        return true;
    }

    public static boolean sendFile(HTTPRequest request, HTTPResponse response, String path) {
        if(!(new File(path)).exists()) return errorNotFound(request, response);

        response.setStatusLine("HTTP/1.1 200 OK");
        response.setHeader("Content-Type", MIMEType.getByFile(path));
        try {
            FileInputStream inputStream = new FileInputStream(path);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = inputStream.read(buffer)) != -1)
                response.addMessage(buffer, 0, n);
        } catch (IOException e) {
            System.err.println("Failed to send file to client");
        }

        return true;
    }

}
