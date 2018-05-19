package nl.jessevogel.photomanager;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.xmp.XmpDirectory;
import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Person;
import nl.jessevogel.photomanager.data.Picture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

class Scanner {

    private Controller controller;

    private ArrayList<String> allowedExtensions;

    private int currentPictureId;
    private int currentAlbumId;
    private int currentPersonId;

    Scanner(Controller controller) {
        this.controller = controller;
        allowedExtensions = new ArrayList<>();
        allowedExtensions.add("jpg");
        allowedExtensions.add("jpeg");
        allowedExtensions.add("png");
    }

    boolean scan() {

        /*
         * Step 1. Index all pictures
         * Step 2. Retreiving metadata
         * Step 3. Create smaller versions / thumbnails
         */

        // Reset counters
        currentPictureId = 0;
        currentAlbumId = 0;
        currentPersonId = 0;

        // Make sure the current root directory is a directory
        File directory = new File(controller.getData().getRootDirectory());
        if (!directory.exists() || !directory.isDirectory()) return false;

        // Clear current data
        Data data = controller.getData();
        data.getAlbums().clear();
        data.getPeople().clear();
        data.getPictures().clear();

        // Scan the root directory
        Log.println("Indexing pictures in " + directory.getAbsolutePath() + " ...");
        boolean success = indexDirectory(directory);
        if(!success) {
            Log.println("Something went wrong while indexing pictures.");
            return false;
        }

        ArrayList<Picture> pictures = controller.getData().getPictures();
        int N = pictures.size();
        Log.println("Found " + N + " pictures!");

        Log.print("Retrieving metadata...   0%");
        int index = 0;
        for (Picture picture : pictures) {
            success = success & getMetaData(picture);
            index++;
            if (index * 100 / N > (index - 1) * 100 / N) Log.updatePercentage(index * 100 / N);
        }
        Log.println("");

        Log.print("Creating thumbnails...   0%");
        index = 0;
        (new File(controller.getData().getThumbsFolder())).mkdir();
        for (Picture picture : pictures) {
            success = success & resizePicture(picture);
            index++;
            if (index * 100 / N > (index - 1) * 100 / N) Log.updatePercentage(index * 100 / N);
        }
        Log.println("");

        Log.println("Scanning complete!");

        return success;
    }

    private boolean indexDirectory(File directory) {
        // Don't scan the data folder or hidden folders
        if (directory.getAbsolutePath().equals(controller.getData().getDataFolder())) return true;
        if (directory.getName().startsWith(".")) return true;

        // Determine directory path
        String directoryPath = directory.getAbsolutePath();
        String rootDirectory = controller.getData().getRootDirectory();
        if (directoryPath.startsWith(rootDirectory) && directoryPath.length() > rootDirectory.length())
            directoryPath = directoryPath.substring(rootDirectory.length() + 1);

        // Go through all files in this directory
        File[] files = directory.listFiles();
        if (files == null) return false;
        Album album = null;
        for (File file : files) {
            if (file.isDirectory()) {
                // Scan every subdirectory
                if (!indexDirectory(file)) {
                    Log.error("Failed to scan directory " + file.getAbsolutePath());
                    return false;
                }
            } else {
                // Only process files that should be processed
                if (!shouldProcessFile(file)) continue;

                // If this directory does not have an associated album, create one
                if (album == null) {
                    album = controller.getData().getAlbumByPath(directoryPath);
                    if (album == null) {
                        album = new Album();
                        album.id = currentAlbumId ++;
                        album.title = file.getParentFile().getName();
                        album.path = directoryPath;
                        controller.getData().getAlbums().add(album);
                    }
                }

                // Create a new picture object
                Picture picture = new Picture();
                picture.id = currentPictureId ++;
                picture.albumId = album.id;
                picture.filename = file.getName();
                album.pictures.add(picture);
                controller.getData().getPictures().add(picture);
            }
        }

        return true;
    }

    private boolean shouldProcessFile(File file) {
        // Check the extension
        String fileName = file.getName();
        int i = fileName.lastIndexOf('.');
        return i >= 0 && allowedExtensions.contains(fileName.substring(i + 1).toLowerCase());
    }

    private boolean getMetaData(Picture picture) {
        // Construct file from picture
        File file = new File(controller.getData().getPicturePath(picture));
        if (!file.exists()) {
            Log.error("For some reason " + file.getAbsolutePath() + " doesn't exist?");
            return false;
        }

        try {
            // Look for people in metadata
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            for (XmpDirectory xmpDirectory : metadata.getDirectoriesOfType(XmpDirectory.class)) {
                for (Map.Entry<String, String> entry : xmpDirectory.getXmpProperties().entrySet()) {
                    if (!entry.getKey().endsWith("mwg-rs:Name")) continue;

                    // Find corresponding person (create one if does not exist)
                    String name = entry.getValue();
                    Person person = controller.getData().getPersonByName(name);
                    if (person == null) {
                        person = new Person();
                        person.id = currentPersonId ++;
                        person.name = name;
                        controller.getData().getPeople().add(person);
                    }
                    person.pictures.add(picture);
                }
            }
        } catch (IOException | ImageProcessingException e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean resizePicture(Picture picture) {
        // Construct file from picture
        File file = new File(controller.getData().getPicturePath(picture));
        if (!file.exists()) {
            Log.error("For some reason " + file.getAbsolutePath() + " doesn't exist?");
            return false;
        }

        // Create smaller version of the picture
        Thumbnailator pictureResizer = new Thumbnailator();
        return pictureResizer.resizeSmall(file.getAbsolutePath(), controller.getData().getThumbPath(picture));
    }

}
