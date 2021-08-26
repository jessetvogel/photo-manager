package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Medium;
import nl.jessevogel.photomanager.data.Person;
import nl.jessevogel.photomanager.httpserver.HTTPRequest;
import nl.jessevogel.photomanager.httpserver.HTTPResponse;
import nl.jessevogel.photomanager.httpserver.HTTPServer;

import java.io.File;
import java.sql.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class APIServer extends HTTPServer {

    private final Controller controller;

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
        if (URIPath.matches("^/media/\\d+$")) return media(request, response);
        if (URIPath.matches("^/media/\\d+/tag$")) return tag(request, response, true);
        if (URIPath.matches("^/media/\\d+/untag$")) return tag(request, response, false);
        if (URIPath.matches("^/people/\\d+/picture$")) return profilePicture(request, response);
        if (URIPath.matches("^/albums/\\d+/cover$")) return cover(request, response);
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
        people.sort((p1, p2) -> (p2.media.size() - p1.media.size()));
        for (Person person : people) {
            if (!first) response.addMessage(",");
            first = false;
            response.addMessage("{"
                    + "\"id\":" + person.id + ","
                    + "\"name\":\"" + person.name + "\","
                    + "\"picture\":" + (new File(controller.getData().getProfilePicturePath(person))).exists()
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

    private boolean media(HTTPRequest request, HTTPResponse response) {
        int pictureId = Integer.parseInt(request.getURIPath().substring(request.getURIPath().lastIndexOf('/') + 1)); // TODO: cleaner?
        Medium medium = controller.getData().getMediumById(pictureId);
        if (medium == null) return errorNotFound(request, response);

        String path;
        String size = request.getQuery("size");
        if (size != null && size.equals("small"))
            path = controller.getData().getThumbPath(medium);
        else
            path = controller.getData().getMediumPath(medium);

        return sendFile(request, response, path);
    }

    private boolean tag(HTTPRequest request, HTTPResponse response, boolean tag) {
        try {
            // Get media
            Pattern pattern = Pattern.compile("^/media/(\\d+)/" + (tag ? "tag" : "untag") + "$");
            Matcher m = pattern.matcher(request.getURIPath());
            if (!m.find()) return errorBadRequest(request, response); // TODO
            int pictureId = Integer.parseInt(m.group(1));
            Medium medium = controller.getData().getMediumById(pictureId);
            if (medium == null) return errorNotFound(request, response);

            if(medium.type != Medium.Type.PHOTO)
                return errorBadRequest(request, response);

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

                // Add or remove media to person
                if(tag)
                    person.media.add(medium);
                else
                    person.media.remove(medium);
            }

            // Tag people in image
            Tagger tagger = new Tagger();
            if(tag) {
                if (!tagger.tagPeople(controller.getData().getMediumPath(medium), people))
                    return errorInternalServerError(request, response);
            }
            else {
                if (!tagger.untagPeople(controller.getData().getMediumPath(medium), people))
                    return errorInternalServerError(request, response);
            }

            // Send response
            response.setStatusLine("HTTP/1.1 200 OK");
            response.setHeader("Content-Type", "application/json");
            response.addMessage("{\"response\":\"successfully " + (tag ? "tagged" : "untagged") + " people in media\"}");
            return true;
        } catch (Exception e) {
            return errorBadRequest(request, response);
        }
    }

    private boolean search(HTTPRequest request, HTTPResponse response) {
        // Determine list of people and albums that we want to search for
        String queryPeople = request.getQuery("people");
        String queryAlbums = request.getQuery("albums");
        if (queryPeople == null && queryAlbums == null) return errorBadRequest(request, response);

        Set<Medium> media = null;
        // Filter for all given people
        if (queryPeople != null) {
            for (String personId : queryPeople.split(",")) {
                try {
                    int id = Integer.parseInt(personId);
                    Person person = controller.getData().getPersonById(id);
                    if (person == null) return errorBadRequest(request, response);
                    if (media == null)
                        media = new HashSet<>(person.media);
                    else
                        media.retainAll(person.media);
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
                    if (media == null)
                        media = new HashSet<>(album.media);
                    else
                        media.retainAll(album.media);
                } catch (NumberFormatException e) {
                    return errorBadRequest(request, response);
                }
            }
        }

        // Construct array list
        ArrayList<Medium> arrayMedia = new ArrayList<>();
        if (media != null) arrayMedia.addAll(media);

        // Sort on name (TODO: sorting options?)
        arrayMedia.sort(Comparator.comparing(p -> p.filename));

        // Determine start and amount
        int size = arrayMedia.size();
        int start = request.getQueryInteger("start", 0);
        int amount = request.getQueryInteger("amount", 10);

        // Send response
        response.setStatusLine("HTTP/1.1 200 OK");
        response.setHeader("Content-Type", "application/json");

        response.addMessage("{");
        response.addMessage("\"hits\":" + size + ",");
        response.addMessage("\"media\":[");

        boolean first = true;
        for (int i = start; i < start + amount && i < size; ++i) {
            if (first) first = false;
            else response.addMessage(",");
            Medium medium = arrayMedia.get(i);
            response.addMessage("{"
                    + "\"id\":" + medium.id + ","
                    + "\"type\":\"" + medium.type.toString().toLowerCase() + "\","
                    + "\"tagged\":[" + controller.getData().getTagged(medium) + "]"
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

        // Pick a random image for the cover
        Medium cover = new ArrayList<>(album.media).get(new Random().nextInt(album.media.size()));
        return sendFile(request, response, controller.getData().getThumbPath(cover));
    }

}
