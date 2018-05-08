package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Person;
import nl.jessevogel.photomanager.data.Picture;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIServer extends HTTPServer {

    private static final Pattern patternHealth = Pattern.compile("^\\/health$");
    private static final Pattern patternPeople = Pattern.compile("^\\/people$");
    private static final Pattern patternAlbums = Pattern.compile("^\\/albums$");
    private static final Pattern patternPicture = Pattern.compile("^\\/pictures\\/(\\d+)");

    private Controller controller;

    APIServer(Controller controller, int port) {
        super(port);
        this.controller = controller;
    }

    @Override
    public boolean respond(HTTPRequest httpRequest) {
        Matcher matcher;

        // /health
        matcher = patternHealth.matcher(httpRequest.URI);
        if (matcher.find()) return health(httpRequest);

        // /people
        matcher = patternPeople.matcher(httpRequest.URI);
        if (matcher.find()) return people(httpRequest);

        // /albums
        matcher = patternAlbums.matcher(httpRequest.URI);
        if (matcher.find()) return albums(httpRequest);

        // /pictures/id
        matcher = patternPicture.matcher(httpRequest.URI);
        if (matcher.find()) return picture(httpRequest, Integer.parseInt(matcher.group(1)));

        // If not found, return 404 Not Found
        return errorNotFound(httpRequest);
    }

    private boolean errorNotFound(HTTPRequest httpRequest) {
        httpRequest.sendLine("HTTP/1.1 404 Not Found");
        httpRequest.sendLine("Content-Type: text/plain");
        httpRequest.sendLine();
        httpRequest.sendLine("Not Found");
        return true;
    }

    // API endpoints
    private boolean health(HTTPRequest httpRequest) {
        httpRequest.sendLine("HTTP/1.1 200 OK");
        httpRequest.sendLine("Content-Type: application/json");
        httpRequest.sendLine();
        httpRequest.sendLine("true");
        return true;
    }

    private boolean people(HTTPRequest httpRequest) {
        httpRequest.sendLine("HTTP/1.1 200 OK");
        httpRequest.sendLine("Content-Type: application/json");
        httpRequest.sendLine();

        httpRequest.send("[");
        boolean first = true;
        for (Person person : controller.getData().getPeople()) {
            if (!first) httpRequest.send(",");
            first = false;
            httpRequest.send("{");
            httpRequest.send("\"id\":" + person.id + ",");
            httpRequest.send("\"name\":\"" + person.name + "\",");
            httpRequest.send("\"profilePictureUrn\":null");
            httpRequest.send("}");
        }
        httpRequest.send("]");
        return true;
    }

    private boolean albums(HTTPRequest httpRequest) {
        httpRequest.sendLine("HTTP/1.1 200 OK");
        httpRequest.sendLine("Content-Type: application/json");
        httpRequest.sendLine();

        httpRequest.send("[");
        boolean first = true;
        for (Album album : controller.getData().getAlbums()) {
            if (!first) httpRequest.send(",");
            first = false;
            httpRequest.send("{");
            httpRequest.send("\"id\":" + album.id + ",");
            httpRequest.send("\"title\":\"" + album.title + "\"");
            httpRequest.send("}");
        }
        httpRequest.send("]");
        return true;
    }

    private boolean picture(HTTPRequest httpRequest, int id) {
        Picture picture = controller.getData().getPictureById(id);
        if (picture == null) return errorNotFound(httpRequest); // TODO ?
        Album album = controller.getData().getAlbumById(picture.albumId);
        String path = controller.getData().getRootDirectory() + album.path + "/" + picture.filename;
        try {
            FileInputStream inputStream = new FileInputStream(path);

            httpRequest.sendLine("HTTP/1.1 200 OK");
            httpRequest.sendLine("Content-Type: image/jpeg");
            httpRequest.sendLine();

            byte[] buffer = new byte[1024]; // Adjust if you want
            int n;
            while ((n = inputStream.read(buffer)) != -1) {
                httpRequest.send(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

}
