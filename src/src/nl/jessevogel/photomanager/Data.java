package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.*;

import java.io.File;
import java.util.ArrayList;

public class Data {

    private final static String DATA_FOLDER = "_data_"; // TODO: maybe change to ".data" or something to make it hidden?
    private final static String PICTURES_FOLDER = "pictures";
    private final static String ALBUMS_FOLDER = "albums";
    private final static String PEOPLE_FOLDER = "people";
    private final static String PICTURES_DATA_FILE = "pictures";
    private final static String ALBUMS_DATA_FILE = "albums";
    private final static String PEOPLE_DATA_FILE = "people";
    private final static String DATA_EXTENSION = "txt"; // TODO: ?

    private Controller controller;

    private String rootDirectory;
    private ArrayList<Picture> pictures;
    private ArrayList<Album> albums;
    private ArrayList<Person> people;

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
        return loadPicturesData() &&
                loadAlbumData() &&
                loadPeopleData();
    }

    boolean storeData() {
        return storePicturesData() &&
                storeAlbumData() &&
                storePeopleData();
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

    private boolean loadAlbumData() {
        DataFile dataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + ALBUMS_FOLDER + "/" + ALBUMS_DATA_FILE + "." + DATA_EXTENSION);
        dataFile.touch();

        ArrayList<Album> albums = new ArrayList<>();
        String line;
        boolean success = true;
        while ((line = dataFile.readLine()) != null) {
            // Each line represents an album
            Album album = new Album();
            if (!album.set(line)) {
                Log.error("Unable to parse line " + dataFile.getLineNumber() + " of albums data file");
                success = false;
                break;
            }
            albums.add(album);

            // Load associations with this album
            DataFile albumDataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + ALBUMS_FOLDER + "/" + album.id + "." + DATA_EXTENSION);
            albumDataFile.touch();
            while((line = albumDataFile.readLine()) != null) {
                int pictureId = Integer.parseInt(line); // TODO: check for NumberFormatException?
                Picture picture = getPictureById(pictureId);
                if(picture == null) return false;
                album.pictures.add(picture);
            }
            albumDataFile.close();
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
            // Write line containing information about this album
            if (!dataFile.writeLine(album.serialize())) {
                success = false;
                break;
            }

            // Write file containing associations with this person
            DataFile albumDataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + ALBUMS_FOLDER + "/" + album.id + "." + DATA_EXTENSION);
            albumDataFile.touch();
            for(Picture picture : album.pictures)
                albumDataFile.writeLine("" + picture.id);
            albumDataFile.close();
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
            // Each line represents a person
            Person person = new Person();
            if (!person.set(line)) {
                Log.error("Unable to parse line " + dataFile.getLineNumber() + " of people data file");
                success = false;
                break;
            }
            people.add(person);

            // Load associations with this person
            DataFile personDataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + PEOPLE_FOLDER + "/" + person.id + "." + DATA_EXTENSION);
            personDataFile.touch();
            while((line = personDataFile.readLine()) != null) {
                int pictureId = Integer.parseInt(line); // TODO: check for NumberFormatException?
                Picture picture = getPictureById(pictureId);
                if(picture == null) return false;
                person.pictures.add(picture);
            }
            personDataFile.close();
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
            // Add line containing information about this person
            if (!dataFile.writeLine(person.serialize())) {
                success = false;
                break;
            }

            // Write file containing associations with this person
            DataFile personDataFile = new DataFile(rootDirectory + "/" + DATA_FOLDER + "/" + PEOPLE_FOLDER + "/" + person.id + "." + DATA_EXTENSION);
            personDataFile.touch();
            for(Picture picture : person.pictures)
                personDataFile.writeLine("" + picture.id);
            personDataFile.close();
        }
        dataFile.close();

        return success;
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

    Person getPersonByName(String name) {
        for (Person person : people) {
            if (person.name.equals(name))
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
