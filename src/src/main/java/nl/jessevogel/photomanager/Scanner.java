package nl.jessevogel.photomanager;

import nl.jessevogel.jfifmetadata.JFIFImage;
import nl.jessevogel.jfifmetadata.JFIFReader;
import nl.jessevogel.jfifmetadata.segments.APP1Segment;
import nl.jessevogel.jfifmetadata.segments.Segment;
import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Person;
import nl.jessevogel.photomanager.data.Picture;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

class Scanner {

    private Controller controller;

    private ArrayList<String> allowedExtensions;

    Scanner(Controller controller) {
        this.controller = controller;
        allowedExtensions = new ArrayList<>();
        allowedExtensions.add("jpg");
        allowedExtensions.add("jpeg");
    }

    boolean scan() {

        /*
         * Step 1. Index all pictures
         * Step 2. Retreiving metadata
         * Step 3. Create smaller versions / thumbnails
         */

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

//        Log.print("Creating thumbnails...   0%");
//        index = 0;
//        (new File(controller.getData().getThumbsFolder())).mkdir();
//        for (Picture picture : pictures) {
//            success = success & resizePicture(picture);
//            index++;
//            if (index * 100 / N > (index - 1) * 100 / N) Log.updatePercentage(index * 100 / N);
//        }
//        Log.println("");

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
                    if (album == null) album = controller.getData().createAlbum(file.getParentFile().getName(), directoryPath);
                }

                // Create a new picture object
                Picture picture = controller.getData().createPicture(album.id, file.getName());
                album.pictures.add(picture);
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

//        try {
//            // Look for people in metadata
//            Metadata metadata = ImageMetadataReader.readMetadata(file);
//            for (XmpDirectory xmpDirectory : metadata.getDirectoriesOfType(XmpDirectory.class)) {
//                for (Map.Entry<String, String> entry : xmpDirectory.getXmpProperties().entrySet()) {
//                    if (!entry.getKey().endsWith("mwg-rs:Name")) continue;
//
//                    // Find corresponding person (create one if does not exist)
//                    String name = entry.getValue();
//                    Person person = controller.getData().getPersonByName(name);
//                    if (person == null) {
//                        person = new Person();
//                        person.id = currentPersonId ++;
//                        person.name = name;
//                        controller.getData().getPeople().add(person);
//                    }
//                    person.pictures.add(picture);
//                }
//            }
//        } catch (IOException | ImageProcessingException e) {
//            e.printStackTrace();
//        }

        try {
            // Read image for metadata
            JFIFReader reader = new JFIFReader();
            JFIFImage image = reader.readMetaData(file.getAbsolutePath());

            // Look for custom data tag
            for(Segment segment : image.getSegments()) {
                // Only consider 0xE1 segments
                if(segment.getMarkerCode() != 0xE1) continue;
                APP1Segment app1Segment = (APP1Segment) segment;
                if(!app1Segment.segmentHeader.equals("photomanager")) continue;

                // Look for people tagged in image
                JSONObject jsonObject = new JSONObject(new String(app1Segment.segmentData, "UTF-8"));
                if(!jsonObject.has("people")) continue;
                JSONArray peopleArray = jsonObject.getJSONArray("people");
                int length = peopleArray.length();
                for(int i = 0;i < length; ++i) {
                    String name = peopleArray.getString(i);
                    Person person = controller.getData().getPersonByName(name);
                    if (person == null) person = controller.getData().createPerson(name);
                    person.pictures.add(picture);
                }
            }
        } catch (Exception e) {
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
