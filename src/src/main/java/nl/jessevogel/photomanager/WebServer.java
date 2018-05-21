package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Person;
import nl.jessevogel.photomanager.data.Picture;
import nl.jessevogel.photomanager.httpserver.HTTPRequest;
import nl.jessevogel.photomanager.httpserver.HTTPResponse;
import nl.jessevogel.photomanager.httpserver.HTTPServer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class WebServer extends HTTPServer {

    private Controller controller;
    private String webDirectory;

    WebServer(Controller controller, int port) {
        super(port);
        this.controller = controller;

        webDirectory = "/Users/jessetvogel/Projects/photo-manager/client"; // TODO
    }

    @Override
    public boolean respond(HTTPRequest request, HTTPResponse response) {
        String URIPath = request.getURIPath();
        if (!validURI(URIPath)) return errorForbidden(request, response);
        if (URIPath.equals("/")) return sendFile(request, response, webDirectory + "/index.html");
        return sendFile(request, response, webDirectory + URIPath);
    }

    private boolean validURI(String uri) {
        if (uri.length() == 0) return false;
        if (uri.charAt(0) != '/') return false;
        if (uri.contains("..")) return false;
        return true;
    }

}
