package nl.jessevogel.photomanager;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HTTPServer {

    private static final int BACKLOG = 10;
    private static final Pattern patternRequestLine = Pattern.compile("^(GET|POST) (.*) (HTTP\\/1.1)$");
    private static final Pattern patternHeader = Pattern.compile("^([\\w\\-]+): (.*)$");

    private int port;
    private ServerSocket serverSocket;
    private boolean acceptingClients;

    public HTTPServer(int port) {
        this.port = port;
    }

    public boolean start() {
        try {
            // Setup server socket and start server thread
            serverSocket = new ServerSocket(port, BACKLOG, InetAddress.getByName("127.0.0.1"));
            acceptingClients = true;
            HTTPServerThread thread = new HTTPServerThread();
            thread.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean stop() {
        try {
            acceptingClients = false;
            serverSocket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public abstract boolean respond(HTTPRequest httpRequest);

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
                e.printStackTrace();
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
                HTTPRequest httpRequest = new HTTPRequest();
                httpRequest.input = clientSocket.getInputStream();
                httpRequest.output = clientSocket.getOutputStream();

                // Parse request line
                BufferedReader br = new BufferedReader(new InputStreamReader(httpRequest.input));
                String requestLine = br.readLine();
                Matcher m = patternRequestLine.matcher(requestLine);
                if (!m.find()) {
                    badRequest(httpRequest);
                    return;
                }
                httpRequest.method = m.group(1);
                httpRequest.URI = m.group(2);
                httpRequest.version = m.group(3);

                // Parse headers
                String headerLine;
                while ((headerLine = br.readLine()).length() > 0) {
                    m = patternHeader.matcher(headerLine);
                    if (!m.find()) {
                        badRequest(httpRequest);
                        return;
                    }
                    httpRequest.headers.put(m.group(1), m.group(2));
                }

                // Respond
                if (!respond(httpRequest)) {
                    badRequest(httpRequest);
                    return;
                }

                // Close socket
                httpRequest.output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void badRequest(HTTPRequest httpRequest) {
            PrintWriter writer = new PrintWriter(httpRequest.output);
            writer.println("HTTP/1.1 400 Bad Request");
            writer.println();
            writer.flush();
            writer.close();
        }
    }

    public static class HTTPRequest {
        public InputStream input;
        public OutputStream output;
        public String method;
        public String URI;
        public String version;
        public Map<String, String> headers;

        public HTTPRequest() {
            headers = new HashMap<>();
        }

        public boolean send(String str) {
            try {
                output.write(str.getBytes());
                output.flush();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        public boolean send(byte[] bytes, int off, int len) {
            try {
                output.write(bytes, off, len);
                output.flush();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        public boolean sendLine(String str) {
            try {
                output.write((str + "\n").getBytes());
                output.flush();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        public boolean sendLine() {
            try {
                output.write('\n');
                output.flush();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

}
