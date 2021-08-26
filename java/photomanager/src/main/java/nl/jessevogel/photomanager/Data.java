package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

class Data {

    private static final String SEPARATOR = File.separator;
    private static final String DATA_FOLDER = ".data";
    private static final String MEDIA_FOLDER = "media";
    private static final String ALBUMS_FOLDER = "albums";
    private static final String PEOPLE_FOLDER = "people";
    private static final String MEDIA_DATA_FILE = "media";
    private static final String ALBUMS_DATA_FILE = "albums";
    private static final String PEOPLE_DATA_FILE = "people";
    private static final String PROFILEPICTURES_FOLDER = "profilepictures";
    private static final String DATA_EXTENSION = "txt";
    private static final String THUMBS_FOLDER = "thumbs";

    private String rootDirectory;
    private ArrayList<Medium> media;
    private ArrayList<Album> albums;
    private ArrayList<Person> people;

    Data() {
        // Default root directory
        rootDirectory = null;

        // Have empty array lists by default
        media = new ArrayList<>();
        albums = new ArrayList<>();
        people = new ArrayList<>();
    }

    boolean setRootDirectory(String directory) {
        // Directory should exist
        File file = new File(directory);
        if (!file.exists() || !file.isDirectory())
            return false;
        rootDirectory = file.getAbsolutePath();
        return true;
    }

    String getRootDirectory() {
        return rootDirectory;
    }

    boolean loadData() {
        if (rootDirectory == null) return false;
        clear();
        return loadMediaData() && loadAlbumData() && loadPeopleData();
    }

    boolean storeData() {
        if (rootDirectory == null) return false;
        return storeMediaData() && storeAlbumData() && storePeopleData();
    }

    private boolean loadMediaData() {
        DataFile dataFile = new DataFile(getMediaDataFile());
        dataFile.touch();

        ArrayList<Medium> list = new ArrayList<>();
        String line;
        boolean success = true;
        while ((line = dataFile.readLine()) != null) {
            if(line.isEmpty()) continue;
            Medium item = new Medium();
            if (!item.set(line)) {
                Log.error("Unable to parse line " + dataFile.getLineNumber() + " of media data file");
                success = false;
                break;
            }
            list.add(item);
        }
        dataFile.close();

        if (success)
            media = list;

        return success;
    }

    private boolean storeMediaData() {
        DataFile dataFile = new DataFile(getMediaDataFile());
        dataFile.touchWrite();
        boolean success = true;
        for (Item item : media) {
            if (!dataFile.writeLine(item.serialize())) {
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

        ArrayList<Album> list = new ArrayList<>();
        String line;
        boolean success = true;
        while ((line = dataFile.readLine()) != null) {
            if(line.isEmpty()) continue;
            // Each line represents an album
            Album album = new Album();
            if (!album.set(line)) {
                Log.error("Unable to parse line " + dataFile.getLineNumber() + " of albums data file");
                success = false;
                break;
            }
            list.add(album);

            // Load associations with this album
            DataFile albumDataFile = new DataFile(getAlbumDataFile(album));
            albumDataFile.touch();
            while ((line = albumDataFile.readLine()) != null) {
                int mediumId = Integer.parseInt(line); // TODO: check for NumberFormatException?
                Medium medium = getMediumById(mediumId);
                if (medium == null) {
                    Log.error("Unknown medium with id " + mediumId);
                    return false;
                }
                album.media.add(medium);
            }
            albumDataFile.close();
        }
        dataFile.close();

        if (success)
            albums = list;

        return success;
    }

    private boolean storeAlbumData() {
        DataFile dataFile = new DataFile(getAlbumsDataFile());
        dataFile.touchWrite();

        boolean success = true;
        for (Album album : albums) {
            // Omit albums without media
            if(album.media.isEmpty())
                continue;

            // Write line containing information about this album
            if (!dataFile.writeLine(album.serialize())) {
                success = false;
                break;
            }

            // Write file containing associations with this person
            DataFile albumDataFile = new DataFile(getAlbumDataFile(album));
            albumDataFile.touch();
            for (Medium medium : album.media)
                albumDataFile.writeLine("" + medium.id);
            albumDataFile.close();
        }
        dataFile.close();

        return success;
    }

    private boolean loadPeopleData() {
        DataFile dataFile = new DataFile(getPeopleDataFile());
        dataFile.touch();

        ArrayList<Person> list = new ArrayList<>();
        String line;
        boolean success = true;
        while ((line = dataFile.readLine()) != null) {
            if(line.isEmpty()) continue;
            // Each line represents a person
            Person person = new Person();
            if (!person.set(line)) {
                Log.error("Unable to parse line " + dataFile.getLineNumber() + " of people data file");
                success = false;
                break;
            }
            list.add(person);

            // Load associations with this person
            DataFile personDataFile = new DataFile(getPersonDataFile(person));
            personDataFile.touch();
            while ((line = personDataFile.readLine()) != null) {
                int mediaId = Integer.parseInt(line); // TODO: check for NumberFormatException?
                Medium medium = getMediumById(mediaId);
                if (medium == null) return false;
                person.media.add(medium);
            }
            personDataFile.close();
        }
        dataFile.close();

        if (success)
            people = list;

        return success;
    }

    private boolean storePeopleData() {
        DataFile dataFile = new DataFile(getPeopleDataFile());
        dataFile.touchWrite();

        boolean success = true;
        for (Person person : people) {
            // Omit people without media
            if(person.media.isEmpty())
                continue;

            // Add line containing information about this person
            if (!dataFile.writeLine(person.serialize())) {
                success = false;
                break;
            }

            // Write file containing associations with this person
            DataFile personDataFile = new DataFile(getPersonDataFile(person));
            personDataFile.touch();
            for (Medium medium : person.media)
                personDataFile.writeLine(String.valueOf(medium.id));
            personDataFile.close();
        }
        dataFile.writeLine("");
        dataFile.close();

        return success;
    }

    ArrayList<Person> getPeople() {
        return people;
    }

    ArrayList<Album> getAlbums() {
        return albums;
    }

    ArrayList<Medium> getMedia() {
        return media;
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

    Medium getMediumById(int id) {
        for (Medium medium : media) {
            if (medium.id == id)
                return medium;
        }
        return null;
    }

    String getProfilePicturePath(Person person) {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + PROFILEPICTURES_FOLDER + SEPARATOR + person.name + ".jpg";
    }

    String getThumbPath(Medium medium) {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + THUMBS_FOLDER + SEPARATOR + medium.id + ".jpg";
    }

    String getMediumPath(Medium medium) {
        Album album = getAlbumById(medium.albumId);
        if (album == null) return null;
        return rootDirectory + SEPARATOR + album.path + SEPARATOR + medium.filename;
    }

    String getDataFolder() {
        return rootDirectory + SEPARATOR + DATA_FOLDER;
    }

    String getThumbsFolder() {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + THUMBS_FOLDER;
    }

    private String getMediaDataFile() {
        return rootDirectory + SEPARATOR + DATA_FOLDER + SEPARATOR + MEDIA_FOLDER + SEPARATOR + MEDIA_DATA_FILE + "." + DATA_EXTENSION;
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

    public Medium createMedium(int albumId, String filename) {
        Medium m = new Medium();
        m.id = 0;
        media.sort(Comparator.comparingInt(n -> n.id));
        while (m.id < media.size() && media.get(m.id).id == m.id)
            m.id++; // find smallest unused id (using that this.media is sorted based on id)
        m.albumId = albumId;
        m.filename = filename;
        media.add(m.id, m); // keeps it sorted
        return m;
    }

    public Person createPerson(String name) {
        Person p = new Person();
        p.id = 0;
        people.sort(Comparator.comparingInt(q -> q.id));
        while (p.id < people.size() && people.get(p.id).id == p.id)
            p.id++; // find smallest unused id (using that this.people is sorted based on id)
        p.name = name;
        people.add(p.id, p);
        return p;
    }

    public Album createAlbum(String title, String path) {
        Album a = new Album();
        a.id = 0;
        albums.sort(Comparator.comparingInt(b -> b.id));
        while (a.id < albums.size() && albums.get(a.id).id == a.id)
            a.id++; // find smallest unused id (using that this.albums is sorted based on id)
        a.title = title;
        a.path = path;
        albums.add(a.id, a);
        return a;
    }

    public void clear() {
        // Clear lists
        albums.clear();
        people.clear();
        media.clear();
    }

    public String getTagged(Medium medium) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (Person person : people) {
            if (person.media.contains(medium)) {
                if (first) first = false;
                else sb.append(',');
                sb.append(person.id);
            }
        }
        return sb.toString();
    }
}
