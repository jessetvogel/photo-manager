package nl.jessevogel.photomanager;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Picture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class Scanner {

    private Controller controller;

    private ArrayList<String> allowedExtensions;

    Scanner(Controller controller) {
        this.controller = controller;
        allowedExtensions = new ArrayList<>();
        allowedExtensions.add("jpg");
        allowedExtensions.add("jpeg");
        allowedExtensions.add("png");
    }

    boolean scan() {
        // Make sure the current root directory is a directory
        File directory = new File(controller.getData().getRootDirectory());
        if(!directory.exists() || !directory.isDirectory()) return false;

        // Clear current data
        Data data = controller.getData();
        data.getAlbums().clear();
        data.getPeople().clear();
        data.getPictures().clear();
        data.getAlbumPictureConnections().clear();
        data.getPersonPictureConnections().clear();

        // Scan the root directory
        return scanDirectory(directory);
    }

    private boolean scanDirectory(File directory) {
        // Determine directory path
        String directoryPath = directory.getAbsolutePath();
        String rootDirectory = controller.getData().getRootDirectory();
        if(directoryPath.startsWith(rootDirectory)) directoryPath = directoryPath.substring(rootDirectory.length());

        // Go through all files in this directory
        File[] files = directory.listFiles();
        if(files == null) return false;
        Album album = null;
        for(File file : files) {
            if(file.isDirectory()) {
                // Scan every subdirectory
                if(!scanDirectory(file)) {
                    Log.error("Failed to scan directory " + file.getAbsolutePath());
                    return false;
                }
            }
            else {
                // Only process files that should be processed
                if(!shouldProcessFile(file)) continue;

                // If this directory does not have an associated album, create one
                if(album == null) {
                    album = controller.getData().getAlbumByPath(directoryPath);
                    if(album == null) {
                        album = new Album();
                        album.id = controller.getData().getAlbums().size();  // TODO
                        album.title = file.getParentFile().getName();
                        album.path = directoryPath;
                        controller.getData().getAlbums().add(album);
                    }
                }

                // Process the file
                if(!processFile(album, file)) {
                    Log.error("Failed to process file " + file.getAbsolutePath());
                    return false;
                }
            }
        }

        return true;
    }

    private boolean shouldProcessFile(File file) {
        // Check the extension
        String fileName = file.getName();
        int i = fileName.lastIndexOf('.');
        return i >= 0 && allowedExtensions.contains(fileName.substring(i + 1));
    }

    private boolean processFile(Album album, File file) {
        // Create a new picture object
        Picture picture = new Picture();
        picture.id = controller.getData().getPictures().size(); // TODO
        picture.albumId = album.id;
        picture.filename = file.getName();
        controller.getData().getPictures().add(picture);

        // Look for people in metadata
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    System.out.println(picture.filename + ": " + tag);
                }
            }
        } catch (IOException | ImageProcessingException e) {
            e.printStackTrace();
        }

        return true;
    }

}
