package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Person;
import nl.jessevogel.photomanager.data.Picture;
import nl.jessevogel.photomanager.httpserver.HTTPRequest;
import nl.jessevogel.photomanager.httpserver.HTTPResponse;
import nl.jessevogel.photomanager.httpserver.HTTPServer;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class APIServer extends HTTPServer {

    private Controller controller;

    APIServer(Controller controller, int port) {
        super(port);
        this.controller = controller;
    }

    @Override
    public boolean respond(HTTPRequest request, HTTPResponse response) {
        // CORS
        String origin = request.getHeader("Origin");
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET");
            response.setHeader("Access-Control-Allow-Headers", "*");
        }

        // Depending on URI, respond accordingly
        String URIPath = request.getURIPath();
        if (URIPath.equals("/")) return home(request, response);
        if (URIPath.equals("/health")) return health(request, response);
        if (URIPath.equals("/people")) return people(request, response);
        if (URIPath.equals("/albums")) return albums(request, response);
        if (URIPath.matches("^\\/pictures\\/\\d+$")) return picture(request, response);
        if (URIPath.matches("^\\/pictures\\/\\d+\\/tag$")) return tag(request, response);
        if (URIPath.matches("^\\/pictures\\/\\d+\\/untag$")) return untag(request, response);
        if (URIPath.matches("^\\/people\\/\\d+\\/profilepicture$")) return profilePicture(request, response);
        if (URIPath.matches("^\\/albums\\/\\d+\\/cover")) return cover(request, response);
        if (URIPath.equals("/people")) return people(request, response);
        if (URIPath.equals("/search")) return search(request, response);

        // If not found, return 404 Not Found
        return errorNotFound(request, response);
    }

    // API endpoints methods

    private boolean home(HTTPRequest request, HTTPResponse response) {
        response.setStatusLine("HTTP/1.1 200 OK");
        response.setHeader("Content-Type", "text/plain");
        response.addMessage("Hello there!");
        return true;
    }

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
        ArrayList<Person> people = controller.getData().getPeople();
        people.sort((p1, p2) -> (p2.pictures.size() - p1.pictures.size()));
        for (Person person : people) {
            if (!first) response.addMessage(",");
            first = false;
            response.addMessage("{"
                    + "\"id\":" + person.id + ","
                    + "\"name\":\"" + person.name + "\","
                    + "\"profilePicture\":" + (new File(controller.getData().getProfilePicturePath(person))).exists()
                    + "}");
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
            response.addMessage("{"
                    + "\"id\":" + album.id + ","
                    + "\"title\":\"" + album.title + "\""
                    + "}");
        }
        response.addMessage("]");
        return true;
    }

    private boolean picture(HTTPRequest request, HTTPResponse response) {
        Integer pictureId = Integer.parseInt(request.getURIPath().substring(request.getURIPath().lastIndexOf('/') + 1)); // TODO: cleaner?
        Picture picture = controller.getData().getPictureById(pictureId);
        if (picture == null) return errorNotFound(request, response);

        String path;
        String size = request.getQuery("size");
        if (size != null && size.equals("small"))
            path = controller.getData().getThumbPath(picture);
        else
            path = controller.getData().getPicturePath(picture);

        return sendFile(request, response, path);
    }

    private boolean tag(HTTPRequest request, HTTPResponse response) {
        try {
            // Get picture
            Pattern pattern = Pattern.compile("^\\/pictures\\/(\\d+)\\/tag$");
            Matcher m = pattern.matcher(request.getURIPath());
            if (!m.find()) return errorBadRequest(request, response); // TODO
            int pictureId = Integer.parseInt(m.group(1));
            Picture picture = controller.getData().getPictureById(pictureId);
            if (picture == null) return errorNotFound(request, response);

            // Get people
            String queryNames = request.getQuery("names");
            if (queryNames == null) return errorBadRequest(request, response);
            queryNames = java.net.URLDecoder.decode(queryNames, "UTF-8");
            String[] names = queryNames.split(",");
            ArrayList<Person> people = new ArrayList<>();
            for (String name : names) {
                // Get or create person by name
                Person person = controller.getData().getPersonByName(name);
                if (person == null) person = controller.getData().createPerson(name);
                people.add(person);

                // Add picture to person
                person.pictures.add(picture);
            }

            // Tag people in image
            Tagger tagger = new Tagger();
            if(!tagger.tagPeople(controller.getData().getPicturePath(picture), people))
                return errorInternalServerError(request, response);

            // Send response
            response.setStatusLine("HTTP/1.1 200 OK");
            response.setHeader("Content-Type", "application/json");
            response.addMessage("{\"response\":\"successfully tagged people in picture\"}");
            return true;
        }
        catch (Exception e) {
            return errorBadRequest(request, response);
        }
    }

    private boolean untag(HTTPRequest request, HTTPResponse response) {
        try {
            // Get picture
            Pattern pattern = Pattern.compile("^\\/pictures\\/(\\d+)\\/untag$");
            Matcher m = pattern.matcher(request.getURIPath());
            if (!m.find()) return errorBadRequest(request, response); // TODO
            int pictureId = Integer.parseInt(m.group(1));
            Picture picture = controller.getData().getPictureById(pictureId);
            if (picture == null) return errorNotFound(request, response);

            // Get people
            String queryNames = request.getQuery("names");
            if (queryNames == null) return errorBadRequest(request, response);
            queryNames = java.net.URLDecoder.decode(queryNames, "UTF-8");
            String[] names = queryNames.split(",");
            ArrayList<Person> people = new ArrayList<>();
            for (String name : names) {
                // Get or create person by name
                Person person = controller.getData().getPersonByName(name);
                if (person == null) person = controller.getData().createPerson(name);
                people.add(person);

                // Remove picture from person
                person.pictures.remove(picture);
            }

            // Tag people in image
            Tagger tagger = new Tagger();
            if(!tagger.untagPeople(controller.getData().getPicturePath(picture), people))
                return errorInternalServerError(request, response);

            // Send response
            response.setStatusLine("HTTP/1.1 200 OK");
            response.setHeader("Content-Type", "application/json");
            response.addMessage("{\"response\":\"successfully untagged people in picture\"}");
            return true;
        }
        catch (Exception e) {
            return errorBadRequest(request, response);
        }
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
        if (pictures != null) arrayPictures.addAll(pictures);

        // Sort on name (TODO: sorting options?)
        arrayPictures.sort(Comparator.comparing(p -> p.filename));

        // Determine start and amount
        int size = arrayPictures.size();
        int start = request.getQueryInteger("start", 0);
        int amount = request.getQueryInteger("amount", 10);

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
            response.addMessage("{"
                    + "\"id\":" + picture.id
                    + "}");
        }

        response.addMessage("]");
        response.addMessage("}");
        return true;
    }

    private boolean profilePicture(HTTPRequest request, HTTPResponse response) {
        String URIPath = request.getURIPath();
        int personId = Integer.parseInt(URIPath.substring(8, URIPath.lastIndexOf('/')));
        Person person = controller.getData().getPersonById(personId);
        if (person == null) return errorNotFound(request, response);

        return sendFile(request, response, controller.getData().getProfilePicturePath(person));
    }

    private boolean cover(HTTPRequest request, HTTPResponse response) {
        String URIPath = request.getURIPath();
        int albumId = Integer.parseInt(URIPath.substring(8, URIPath.lastIndexOf('/')));
        Album album = controller.getData().getAlbumById(albumId);
        if (album == null) return errorNotFound(request, response);

        return sendFile(request, response, controller.getData().getThumbPath(album.pictures.iterator().next()));
    }

}
