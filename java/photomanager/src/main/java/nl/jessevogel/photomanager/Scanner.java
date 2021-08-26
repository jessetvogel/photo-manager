package nl.jessevogel.photomanager;

import nl.jessevogel.jfifmetadata.JFIFImage;
import nl.jessevogel.jfifmetadata.JFIFReader;
import nl.jessevogel.jfifmetadata.segments.APP1Segment;
import nl.jessevogel.jfifmetadata.segments.Segment;
import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Medium;
import nl.jessevogel.photomanager.data.Person;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;

class Scanner {

    private final Controller controller;

    Scanner(Controller controller) {
        this.controller = controller;
    }

    boolean scan() {
        // Make sure the current root directory is a directory
        String rootDirectory = controller.getData().getRootDirectory();
        if (rootDirectory == null) return false;
        File directory = new File(rootDirectory);
        if (!directory.exists() || !directory.isDirectory()) return false;

        /*
         * Step 1. Check if pictures are deleted
         * Step 2. Search through directories for (new) pictures
         * Step 3. Retrieving metadata
         * Step 4. Create smaller versions / thumbnails
         */

        // Check if pictures are deleted
        Log.println("Scan for deleted pictures");
        HashSet<Medium> removedMedia = new HashSet<>();
        for (Medium medium : controller.getData().getMedia()) {
            String picturePath = controller.getData().getMediumPath(medium);
            if (picturePath == null || !(new File(picturePath)).exists())
                removedMedia.add(medium);
        }
        controller.getData().getMedia().removeAll(removedMedia);

        // If pictures were deleted, remove them from people and albums
        int N = removedMedia.size();
        Log.println("Found " + N + " deleted media(s)");
        if (N > 0) {
            HashSet<Person> removedPeople = new HashSet<>();
            HashSet<Album> removedAlbums = new HashSet<>();
            for (Person person : controller.getData().getPeople()) {
                person.media.removeAll(removedMedia);
                if (person.media.size() == 0) removedPeople.add(person);
            }
            for (Album album : controller.getData().getAlbums()) {
                album.media.removeAll(removedMedia);
                if (album.media.size() == 0) removedAlbums.add(album);
            }
            controller.getData().getPeople().removeAll(removedPeople);
            controller.getData().getAlbums().removeAll(removedAlbums);
        }

        // Scan the root directory for new pictures
        Log.println("Scan for new pictures in " + directory.getAbsolutePath());
        ArrayList<Medium> media = new ArrayList<>();
        boolean success = indexDirectory(directory, media);
        if (!success) {
            Log.println("Something went wrong while scanning for new pictures.");
            return false;
        }

        N = media.size();
        Log.println("Found " + N + " new media(s)");
        if (N == 0) return true;

        // In case new pictures are found, do get metadata and make thumbnails
        Log.print("Retrieving metadata...   0%");
        int index = 0;
        for (Medium medium : media) {
            if(medium.type == Medium.Type.PHOTO)
                success = success & getMetaData(medium);
            index++;
            if (index * 100 / N > (index - 1) * 100 / N) Log.updatePercentage(index * 100 / N);
        }
        Log.println("");

        File thumbsFolder = new File(controller.getData().getThumbsFolder());
        if (!thumbsFolder.exists()) {
            Log.println("Creating thumbnails folder");
            if (!thumbsFolder.mkdir())
                return false;
        }

        Log.print("Creating thumbnails...   0%");
        index = 0;
        for (Medium medium : media) {
            if(!createThumbMedium(medium)) {
                Log.error("Failed to create thumbnail for " + controller.getData().getMediumPath(medium));
                success = false;
            }
            index++;
            if (index * 100 / N > (index - 1) * 100 / N) Log.updatePercentage(index * 100 / N);
        }
        Log.println("");

        return success;
    }

    private boolean indexDirectory(File directory, ArrayList<Medium> media) {
        // Don't scan the data folder
        if (directory.getAbsolutePath().equals(controller.getData().getDataFolder())) return true;
        // Don't scan hidden folders (but allow for ..)
        String directoryName = directory.getName();
        if (directoryName.startsWith(".") && !directoryName.startsWith("..")) return true;

        // Determine directory path
        String directoryPath = directory.getAbsolutePath();
        String rootDirectory = controller.getData().getRootDirectory();
        if (!directoryPath.startsWith(rootDirectory)) return true; // TODO: this should not be possible right?
        if (directoryPath.equals(rootDirectory))
            directoryPath = "";
        else
            directoryPath = directoryPath.substring(rootDirectory.length() + 1);

        // Go through all files in this directory
        File[] files = directory.listFiles();
        if (files == null) return false;
        Album album = null;

        for (File file : files) {
            if (file.isDirectory()) {
                // Scan every subdirectory
                if (!indexDirectory(file, media)) {
                    Log.error("Failed to scan directory " + file.getAbsolutePath());
                    return false;
                }
            } else {
                // Do not index files from root directory
                if (directoryPath.length() == 0) continue;

                // Only process files that should be processed
                if (!shouldProcessFile(file)) continue;

                // If this directory does not have an associated album, create one
                if (album == null) {
                    album = controller.getData().getAlbumByPath(directoryPath);
                    if (album == null)
                        album = controller.getData().createAlbum(file.getParentFile().getName(), directoryPath);
                }

                // Check if this media is already scanned ...
                boolean isNew = true;
                for (Medium p : album.media) {
                    if (p.filename.equals(file.getName())) {
                        isNew = false;
                        break;
                    }
                }

                // ... if not, create a new media object
                if (isNew) {
                    Medium medium = controller.getData().createMedium(album.id, file.getName());
                    medium.type = Medium.typeFromExtension(Medium.getFileExtension(file.getName()));
                    album.media.add(medium);
                    media.add(medium);
                }
            }
        }

        return true;
    }

    private boolean shouldProcessFile(File file) {
        // Don't allow for hidden files
        String filename = file.getName();
        if (filename.startsWith(".")) return false;

        // Check the extension
        return Medium.typeFromExtension(Medium.getFileExtension(filename)) != null;
    }

    private boolean getMetaData(Medium medium) {
        // Construct file from media
        File file = new File(controller.getData().getMediumPath(medium));
        if (!file.exists()) {
            Log.error("For some reason " + file.getAbsolutePath() + " doesn't exist?");
            return false;
        }

        try {
            // Read image for metadata
            JFIFReader reader = new JFIFReader();
            JFIFImage image = reader.readMetaData(file.getAbsolutePath());

            // Look for custom data tag
            for (Segment segment : image.getSegments()) {
                // Only consider 0xE1 segments
                if (segment.getMarkerCode() != 0xE1) continue;
                APP1Segment app1Segment = (APP1Segment) segment;
                if (!app1Segment.segmentHeader.equals("photomanager")) continue;

                // Look for people tagged in image
                JSONObject jsonObject = new JSONObject(new String(app1Segment.segmentData, StandardCharsets.UTF_8));
                if (!jsonObject.has("people")) continue;
                JSONArray peopleArray = jsonObject.getJSONArray("people");
                int length = peopleArray.length();
                for (int i = 0; i < length; ++i) {
                    String name = peopleArray.getString(i);
                    Person person = controller.getData().getPersonByName(name);
                    if (person == null) person = controller.getData().createPerson(name);
                    person.media.add(medium);
                }
            }
        } catch (Exception e) {
            Log.error("Failed to extract metadata from " + file.getAbsolutePath());
        }

        return true;
    }

    private boolean createThumbMedium(Medium medium) {
        // Construct file from media
        File file = new File(controller.getData().getMediumPath(medium));
        if (!file.exists()) {
            Log.error("For some reason " + file.getAbsolutePath() + " doesn't exist?");
            return false;
        }

        // Create smaller version of the media
        Thumbnailator t = new Thumbnailator();

        if(medium.type == Medium.Type.PHOTO)
            return t.thumbnailPhoto(file.getAbsolutePath(), controller.getData().getThumbPath(medium));

        if(medium.type == Medium.Type.VIDEO)
            return t.thumbnailVideo(file.getAbsolutePath(), controller.getData().getThumbPath(medium));

        Log.error("Unknown media type " + medium.type.toString().toLowerCase());
        return false;
    }
}
