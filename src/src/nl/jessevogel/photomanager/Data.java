package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.*;

import java.io.File;
import java.util.ArrayList;

public class Data {

    private final static String DATA_FOLDER = "_data_"; // TODO: maybe change to ".data" or something to make it hidden?
    private final static String ALBUMS_FOLDER = "albums";
    private final static String PEOPLE_FOLDER = "people";
    private final static String PICTURES_FOLDER = "pictures";
    private final static String ALBUMS_DATA_FILE = "albums";
    private final static String PEOPLE_DATA_FILE = "people";
    private final static String PICTURES_DATA_FILE = "pictures";
    private final static String DATA_EXTENSION = "txt"; // TODO: ?

    private Controller controller;

    private String rootDirectory;
    private ArrayList<Album> albums;
    private ArrayList<Person> people;
    private ArrayList<Picture> pictures;
    private ArrayList<AlbumPictureConnection> albumPictureConnections;
    private ArrayList<PersonPictureConnection> personPictureConnections;

    Data(Controller controller) {
        this.controller = controller;
        rootDirectory = "/Users/jessetvogel/Desktop/test";
    }

    boolean setRootDirectory(String directory) {
        File file = new File(directory);
        if (!file.exists() || !file.isDirectory())
            return false;
        rootDirectory = directory;
        return true;
    }

    String getRootDirectory() {
        return rootDirectory;
    }

    boolean loadData() {
        return loadAlbumData() &&
                loadPeopleData() &&
                loadPicturesData() &&
                loadAlbumPictureConnections() &&
                loadPersonPictureConnections();
    }

    boolean storeData() {
        return storeAlbumData() &&
                storePeopleData() &&
                storePicturesData() &&
                storeAlbumPictureConnections() &&
                storePersonPictureConnections();
    }

    private boolean loadAlbumData() {
        DataFile dataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + ALBUMS_FOLDER + "/" + ALBUMS_DATA_FILE + "." + DATA_EXTENSION);
        dataFile.touch();

        ArrayList<Album> albums = new ArrayList<>();
        String line;
        boolean success = true;
        while ((line = dataFile.readLine()) != null) {
            Album album = new Album();
            if (!album.set(line)) {
                Log.error("Unable to parse line " + dataFile.getLineNumber() + " of albums data file");
                success = false;
                break;
            }
            albums.add(album);
        }
        dataFile.close();

        if (success)
            this.albums = albums;

        return success;
    }

    private boolean storeAlbumData() {
        DataFile dataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + ALBUMS_FOLDER + "/" + ALBUMS_DATA_FILE + "." + DATA_EXTENSION);
        dataFile.touch();

        boolean success = true;
        for (Album album : albums) {
            if (!dataFile.writeLine(album.serialize())) {
                success = false;
                break;
            }
        }
        dataFile.close();

        return success;
    }

    private boolean loadPeopleData() {
        DataFile dataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + PEOPLE_FOLDER + "/" + PEOPLE_DATA_FILE + "." + DATA_EXTENSION);
        dataFile.touch();

        ArrayList<Person> people = new ArrayList<>();
        String line;
        boolean success = true;
        while ((line = dataFile.readLine()) != null) {
            Person person = new Person();
            if (!person.set(line)) {
                Log.error("Unable to parse line " + dataFile.getLineNumber() + " of people data file");
                success = false;
                break;
            }
            people.add(person);
        }
        dataFile.close();

        if (success)
            this.people = people;

        return success;
    }

    private boolean storePeopleData() {
        DataFile dataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + PEOPLE_FOLDER + "/" + PEOPLE_DATA_FILE + "." + DATA_EXTENSION);
        dataFile.touch();

        boolean success = true;
        for (Person person : people) {
            if (!dataFile.writeLine(person.serialize())) {
                success = false;
                break;
            }
        }
        dataFile.close();

        return success;
    }

    private boolean loadPicturesData() {
        DataFile dataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + PICTURES_FOLDER + "/" + PICTURES_DATA_FILE + "." + DATA_EXTENSION);
        dataFile.touch();

        ArrayList<Picture> pictures = new ArrayList<>();
        String line;
        boolean success = true;
        while ((line = dataFile.readLine()) != null) {
            Picture picture = new Picture();
            if (!picture.set(line)) {
                Log.error("Unable to parse line " + dataFile.getLineNumber() + " of pictures data file");
                success = false;
                break;
            }
            pictures.add(picture);
        }
        dataFile.close();

        if (success)
            this.pictures = pictures;

        return success;
    }

    private boolean storePicturesData() {
        DataFile dataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + PICTURES_FOLDER + "/" + PICTURES_DATA_FILE + "." + DATA_EXTENSION);
        dataFile.touch();

        boolean success = true;
        for (Picture picture : pictures) {
            if (!dataFile.writeLine(picture.serialize())) {
                success = false;
                break;
            }
        }
        dataFile.close();

        return success;
    }

    private boolean loadPersonPictureConnections() {
        File peopleDirectory = new File(rootDirectory + "/" + DATA_FOLDER + "/" + PEOPLE_FOLDER);
        // TODO: check if directory exists?

        File[] files = peopleDirectory.listFiles();
        if (files == null) return false;
        ArrayList<PersonPictureConnection> connections = new ArrayList<>();
        for (File file : files) {
            String fileName = file.getName();
            if (!fileName.matches("\\d+\\." + DATA_EXTENSION)) continue;
            int personId = Integer.parseInt(fileName.substring(0, fileName.indexOf('.')));
            Person person = controller.getData().getPersonById(personId);
            if (person == null) {
                Log.warning("Found file " + fileName + " in people folder but no person exists with that id");
                continue;
            }

            PersonPictureConnection connection = new PersonPictureConnection();
            connection.person = person;
            DataFile dataFile = new DataFile(file.getAbsolutePath());
            String line;
            while((line = dataFile.readLine()) != null) {
                int pictureId = Integer.parseInt(line); // TODO: check for NumberFormatException?
                Picture picture = getPictureById(pictureId);
                if(picture == null) return false;
                connection.pictures.add(picture);
            }
            dataFile.close();

            connections.add(connection);
        }

        personPictureConnections = connections;
        return true;
    }

    private boolean storePersonPictureConnections() {
        for(PersonPictureConnection connection : personPictureConnections) {
            int personId = connection.person.id;
            DataFile dataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + PEOPLE_FOLDER + "/" + personId + "." + DATA_EXTENSION);
            dataFile.touch();
            for(Picture picture : connection.pictures)
                dataFile.writeLine("" + picture.id);
            dataFile.close();
        }

        return true;
    }

    private boolean loadAlbumPictureConnections() {
        File albumDirectory = new File(rootDirectory + "/" + DATA_FOLDER + "/" + ALBUMS_FOLDER);
        // TODO: check if directory exists?

        File[] files = albumDirectory.listFiles();
        if (files == null) return false;
        ArrayList<AlbumPictureConnection> connections = new ArrayList<>();
        for (File file : files) {
            String fileName = file.getName();
            if (!fileName.matches("\\d+\\." + DATA_EXTENSION)) continue;
            int albumId = Integer.parseInt(fileName.substring(0, fileName.indexOf('.')));
            Album album = controller.getData().getAlbumById(albumId);
            if (album == null) {
                Log.warning("Found file " + fileName + " in album folder but no album exists with that id");
                continue;
            }

            AlbumPictureConnection connection = new AlbumPictureConnection();
            connection.album = album;
            DataFile dataFile = new DataFile(file.getAbsolutePath());
            String line;
            while((line = dataFile.readLine()) != null) {
                int pictureId = Integer.parseInt(line); // TODO: check for NumberFormatException?
                Picture picture = getPictureById(pictureId);
                if(picture == null) return false;
                connection.pictures.add(picture);
            }
            dataFile.close();

            connections.add(connection);
        }

        albumPictureConnections = connections;
        return true;
    }

    private boolean storeAlbumPictureConnections() {
        for(AlbumPictureConnection connection : albumPictureConnections) {
            int albumId = connection.album.id;
            DataFile dataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + ALBUMS_FOLDER + "/" + albumId + "." + DATA_EXTENSION);
            dataFile.touch();
            for(Picture picture : connection.pictures)
                dataFile.writeLine("" + picture.id);
            dataFile.close();
        }

        return true;
    }

    ArrayList<Person> getPeople() {
        return people;
    }

    ArrayList<Album> getAlbums() {
        return albums;
    }

    ArrayList<Picture> getPictures() {
        return pictures;
    }

    ArrayList<AlbumPictureConnection> getAlbumPictureConnections() {
        return albumPictureConnections;
    }

    ArrayList<PersonPictureConnection> getPersonPictureConnections() {
        return personPictureConnections;
    }

    Album getAlbumByPath(String path) {
        for (Album album : albums) {
            if (album.path.equals(path))
                return album;
        }
        return null;
    }

    Album getAlbumById(int id) {
        for (Album album : albums) {
            if (album.id == id)
                return album;
        }
        return null;
    }

    Person getPersonById(int id) {
        for (Person person : people) {
            if (person.id == id)
                return person;
        }
        return null;
    }

    Picture getPictureById(int id) {
        for (Picture picture : pictures) {
            if (picture.id == id)
                return picture;
        }
        return null;
    }

}
