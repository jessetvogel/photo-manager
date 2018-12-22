package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.httpserver.HTTPRequest;
import nl.jessevogel.photomanager.httpserver.HTTPResponse;
import nl.jessevogel.photomanager.httpserver.HTTPServer;

class WebServer extends HTTPServer {

    private static final String WEB_DIRECTORY = "client";

    WebServer(int port) {
        super(port);
    }

    @Override
    public boolean respond(HTTPRequest request, HTTPResponse response) {
        String URIPath = request.getURIPath();
        if (!validURI(URIPath)) return errorBadRequest(request, response);
        if (URIPath.equals("/")) return sendFile(request, response, WEB_DIRECTORY + "/index.html");
        return sendFile(request, response, WEB_DIRECTORY + URIPath);
    }

    private boolean validURI(String uri) {
        // URI may not be empty, must start with '/' and may not contain '..'
        if (uri.length() == 0) return false;
        if (uri.charAt(0) != '/') return false;
        if (uri.contains("..")) return false;

        // other than that, regard it as valid
        return true;
    }

}
