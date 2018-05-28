package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.*;

import java.io.File;
import java.util.ArrayList;

class Data {

    private static final String SEPARATOR = File.separator;
    private static final String DATA_FOLDER = ".data";
    private static final String PICTURES_FOLDER = "pictures";
    private static final String ALBUMS_FOLDER = "albums";
    private static final String PEOPLE_FOLDER = "people";
    private static final String PICTURES_DATA_FILE = "pictures";
    private static final String ALBUMS_DATA_FILE = "albums";
    private static final String PEOPLE_DATA_FILE = "people";
    private static final String PROFILEPICTURES_FOLDER = "profilepictures";
    private static final String DATA_EXTENSION = "txt"; // TODO: ?
    private static final String THUMBS_FOLDER = "thumbs";

    private String rootDirectory;
    private ArrayList<Picture> pictures;
    private ArrayList<Album> albums;
    private ArrayList<Person> people;

    private int currentPictureId;
    private int currentAlbumId;
    private int currentPersonId;

    Data() {
        // Default root directory TODO: change this
        rootDirectory = "/Users/jessetvogel/Desktop/test";

        // Initialize id counters
        currentPictureId = 0;
        currentAlbumId = 0;
        currentPersonId = 0;
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
        DataFile dataFile = new DataFile(getPicturesDataFile());
        dataFile.touch();

        ArrayList<Picture> pictures = new ArrayList<>();
        String line;
        boolean success = true;
        int maxPictureId = -1;
        while ((line = dataFile.readLine()) != null) {
            Picture picture = new Picture();
            if (!picture.set(line)) {
                Log.error("Unable to parse line " + dataFile.getLineNumber() + " of pictures data file");
                success = false;
                break;
            }
            pictures.add(picture);

            maxPictureId = Math.max(maxPictureId, picture.id);
        }
        dataFile.close();

        if (success) {
            this.pictures = pictures;
            currentPictureId = Math.max(currentPictureId, maxPictureId + 1);
        }

        return success;
    }

    private boolean storePicturesData() {
        DataFile dataFile = new DataFile(getPicturesDataFile());
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
        DataFile dataFile = new DataFile(getAlbumsDataFile());
        dataFile.touch();

        ArrayList<Album> albums = new ArrayList<>();
        String line;
        boolean success = true;
        int maxAlbumId = -1;
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
            DataFile albumDataFile = new DataFile(getAlbumDataFile(album));
            albumDataFile.touch();
            while((line = albumDataFile.readLine()) != null) {
                int pictureId = Integer.parseInt(line); // TODO: check for NumberFormatException?
                Picture picture = getPictureById(pictureId);
                if(picture == null) return false;
                album.pictures.add(picture);
            }
            albumDataFile.close();

            // Keep track of max album id
            maxAlbumId = Math.max(maxAlbumId, album.id);
        }
        dataFile.close();

        if (success) {
            this.albums = albums;
            currentAlbumId = Math.max(currentAlbumId, maxAlbumId + 1);
        }

        return success;
    }

    private boolean storeAlbumData() {
        DataFile dataFile = new DataFile(getAlbumsDataFile());
        dataFile.touch();

        boolean success = true;
        for (Album album : albums) {
            // Write line containing information about this album
            if (!dataFile.writeLine(album.serialize())) {
                success = false;
                break;
            }

            // Write file containing associations with this person
            DataFile albumDataFile = new DataFile(getAlbumDataFile(album));
            albumDataFile.touch();
            for(Picture picture : album.pictures)
                albumDataFile.writeLine("" + picture.id);
            albumDataFile.close();
        }
        dataFile.close();

        return success;
    }

    private boolean loadPeopleData() {
        DataFile dataFile = new DataFile(getPeopleDataFile());
        dataFile.touch();

        ArrayList<Person> people = new ArrayList<>();
        String line;
        boolean success = true;
        int maxPersonId = -1;
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
            DataFile personDataFile = new DataFile(getPersonDataFile(person));
            personDataFile.touch();
            while((line = personDataFile.readLine()) != null) {
                int pictureId = Integer.parseInt(line); // TODO: check for NumberFormatException?
                Picture picture = getPictureById(pictureId);
                if(picture == null) return false;
                person.pictures.add(picture);
            }
            personDataFile.close();

            // Keep track of max person id
            maxPersonId = Math.max(maxPersonId, person.id);
        }
        dataFile.close();

        if (success) {
            this.people = people;
            currentPersonId = Math.max(currentPersonId, maxPersonId + 1);
        }

        return success;
    }

    private boolean storePeopleData() {
        DataFile dataFile = new DataFile(getPeopleDataFile());
        dataFile.touch();

        boolean success = true;
        for (Person person : people) {
            // Add line containing information about this person
            if (!dataFile.writeLine(person.serialize())) {
                success = false;
                break;
            }

            // Write file containing associations with this person
            DataFile personDataFile = new DataFile(getPersonDataFile(person));
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

    String getProfilePicturePath(Person person) {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + PROFILEPICTURES_FOLDER + SEPARATOR + person.name + ".jpg";
    }

    String getThumbPath(Picture picture) {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + THUMBS_FOLDER + SEPARATOR + picture.id + ".jpg";
    }

    String getPicturePath(Picture picture) {
        return rootDirectory + SEPARATOR + getAlbumById(picture.albumId).path + SEPARATOR + picture.filename;
    }

    String getDataFolder() {
        return rootDirectory + SEPARATOR + DATA_FOLDER;
    }

    String getThumbsFolder() {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + THUMBS_FOLDER;
    }

    private String getPicturesDataFile() {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + PICTURES_FOLDER + SEPARATOR + PICTURES_DATA_FILE + "." + DATA_EXTENSION;
    }

    private String getAlbumsDataFile() {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + ALBUMS_FOLDER + SEPARATOR + ALBUMS_DATA_FILE + "." + DATA_EXTENSION;
    }

    private String getAlbumDataFile(Album album) {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + ALBUMS_FOLDER + SEPARATOR + album.id + "." + DATA_EXTENSION;
    }

    private String getPeopleDataFile() {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + PEOPLE_FOLDER + SEPARATOR + PEOPLE_DATA_FILE + "." + DATA_EXTENSION;
    }

    private String getPersonDataFile(Person person) {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + PEOPLE_FOLDER + SEPARATOR + person.id + "." + DATA_EXTENSION;
    }

    public Picture createPicture(int albumId, String filename) {
        Picture picture = new Picture();
        picture.id = currentPictureId ++;
        picture.albumId = albumId;
        picture.filename = filename;
        pictures.add(picture);
        return picture;
    }

    public Person createPerson(String name) {
        Person person = new Person();
        person.id = currentPersonId ++;
        person.name = name;
        people.add(person);
        return person;
    }

    public Album createAlbum(String title, String path) {
        Album album = new Album();
        album.id = currentAlbumId ++;
        album.title = title;
        album.path = path;
        albums.add(album);
        return album;
    }
}
