package nl.jessevogel.photomanager.httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HTTPResponse {
    private String statusLine;
    private Map<String, String> headers;
    private ByteArrayOutputStream message;

    public HTTPResponse() {
        headers = new HashMap<>();
        message = new ByteArrayOutputStream();
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public boolean addMessage(String s) {
        try {
            message.write(s.getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean addMessage(byte[] b, int off, int len) {
        message.write(b, off, len);
        return true;
    }

    public boolean send(OutputStream outputStream) {
        try {
            // Write status line
            outputStream.write((statusLine + "\n").getBytes());

            // Write headers
            for (Map.Entry<String, String> entry : headers.entrySet())
                outputStream.write((entry.getKey() + ": " + entry.getValue() + "\n").getBytes());
            outputStream.write('\n');

            // Write message
            message.writeTo(outputStream);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
