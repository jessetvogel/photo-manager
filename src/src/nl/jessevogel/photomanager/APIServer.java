package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Person;
import nl.jessevogel.photomanager.data.Picture;
import nl.jessevogel.photomanager.httpserver.HTTPRequest;
import nl.jessevogel.photomanager.httpserver.HTTPResponse;
import nl.jessevogel.photomanager.httpserver.HTTPServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class APIServer extends HTTPServer {

    private Controller controller;

    APIServer(Controller controller, int port) {
        super(port);
        this.controller = controller;
    }

    @Override
    public boolean respond(HTTPRequest request, HTTPResponse response) {
        // CORS
        String origin = request.getHeader("Origin");
        if(origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET");
            response.setHeader("Access-Control-Allow-Headers", "*");
        }

        String URIPath = request.getURIPath();
        if (URIPath.equals("/health")) return health(request, response);
        if (URIPath.equals("/people")) return people(request, response);
        if (URIPath.equals("/albums")) return albums(request, response);
        if (URIPath.matches("^\\/pictures\\/(\\d+)$")) return picture(request, response);
        if (URIPath.equals("/search")) return search(request, response);

        // If not found, return 404 Not Found
        return errorNotFound(request, response);
    }

    // API endpoints

    private boolean health(HTTPRequest request, HTTPResponse response) {
        response.setStatusLine("HTTP/1.1 200 OK");
        response.setHeader("Content-Type", "application/json");
        response.addMessage("true");
        return true;
    }

    private boolean people(HTTPRequest request, HTTPResponse response) {
        response.setStatusLine("HTTP/1.1 200 OK");
        response.setHeader("Content-Type", "application/json");
        response.addMessage("[");
        boolean first = true;
        for (Person person : controller.getData().getPeople()) {
            if (!first) response.addMessage(",");
            first = false;
            response.addMessage("{");
            response.addMessage("\"id\":" + person.id + ",");
            response.addMessage("\"name\":\"" + person.name + "\",");
            response.addMessage("\"profilePictureUrn\":null");
            response.addMessage("}");
        }
        response.addMessage("]");
        return true;
    }

    private boolean albums(HTTPRequest request, HTTPResponse response) {
        response.setStatusLine("HTTP/1.1 200 OK");
        response.setHeader("Content-Type", "application/json");
        response.addMessage("[");
        boolean first = true;
        for (Album album : controller.getData().getAlbums()) {
            if (!first) response.addMessage(",");
            first = false;
            response.addMessage("{");
            response.addMessage("\"id\":" + album.id + ",");
            response.addMessage("\"title\":\"" + album.title + "\"");
            response.addMessage("}");
        }
        response.addMessage("]");
        return true;
    }

    private boolean picture(HTTPRequest request, HTTPResponse response) {
        Integer id = Integer.parseInt(request.getURIPath().substring(request.getURIPath().lastIndexOf('/') + 1)); // TODO: cleaner?
        Picture picture = controller.getData().getPictureById(id);
        if (picture == null) return errorNotFound(request, response); // TODO ?
        Album album = controller.getData().getAlbumById(picture.albumId);
        String path;

        String size = request.getQuery("size");
        if(size != null && size.equals("small"))
            path = controller.getData().getRootDirectory() + "/_data_/thumb/" + picture.id + ".jpg";
        else
            path = controller.getData().getRootDirectory() + album.path + "/" + picture.filename;

        response.setStatusLine("HTTP/1.1 200 OK");
        response.setHeader("Content-Type", "image/jpeg");
        try {
            FileInputStream inputStream = new FileInputStream(path);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = inputStream.read(buffer)) != -1)
                response.addMessage(buffer, 0, n);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean search(HTTPRequest request, HTTPResponse response) {
        // Determine list of people and albums that we want to search for
        String queryPeople = request.getQuery("people");
        String queryAlbums = request.getQuery("albums");
        if (queryPeople == null && queryAlbums == null) return errorBadRequest(request, response);

        Set<Picture> pictures = null;
        // Filter for all given people
        if (queryPeople != null) {
            for (String personId : queryPeople.split(",")) {
                try {
                    int id = Integer.parseInt(personId);
                    Person person = controller.getData().getPersonById(id);
                    if (person == null) return errorBadRequest(request, response);
                    if (pictures == null)
                        pictures = new HashSet<>(person.pictures);
                    else
                        pictures.retainAll(person.pictures);
                } catch (NumberFormatException e) {
                    return errorBadRequest(request, response);
                }
            }
        }

        // Filter for all given albums
        if (queryAlbums != null) {
            for (String albumId : queryAlbums.split(",")) {
                try {
                    int id = Integer.parseInt(albumId);
                    Album album = controller.getData().getAlbumById(id);
                    if (album == null) return errorBadRequest(request, response);
                    if (pictures == null)
                        pictures = new HashSet<>(album.pictures);
                    else
                        pictures.retainAll(album.pictures);
                } catch (NumberFormatException e) {
                    return errorBadRequest(request, response);
                }
            }
        }

        // Construct array list
        ArrayList<Picture> arrayPictures = new ArrayList<>();
        arrayPictures.addAll(pictures);
        int size = arrayPictures.size();

        // Determine start and amount
        int start = 0, amount = 10;
        try {
            start = Integer.parseInt(request.getQuery("start"));
        } catch (NumberFormatException e) {
        }
        try {
            amount = Integer.parseInt(request.getQuery("amount"));
        } catch (NumberFormatException e) {
        }

        // Send response
        response.setStatusLine("HTTP/1.1 200 OK");
        response.setHeader("Content-Type", "application/json");

        response.addMessage("{");
        response.addMessage("\"amountOfPictures\":" + size + ",");
        response.addMessage("\"pictures\":[");

        boolean first = true;
        for (int i = start; i < start + amount && i < size; ++i) {
            if (first) first = false;
            else response.addMessage(",");
            Picture picture = arrayPictures.get(i);
            response.addMessage("{\"id\":" + picture.id + "}");
        }

        response.addMessage("]");
        response.addMessage("}");
        return true;
    }

}
