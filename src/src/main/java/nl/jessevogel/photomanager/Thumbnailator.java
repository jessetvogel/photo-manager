package nl.jessevogel.photomanager;

import java.io.IOException;

class Thumbnailator {

    private static final int MAX_SIZE_SMALL = 720;

    boolean resizeSmall(String sourcePath, String destinationPath) {
        return resize(sourcePath, destinationPath, MAX_SIZE_SMALL);
    }

    private boolean resize(String sourcePath, String destinationPath, int size) {
        // Resize using library
        try {
            net.coobird.thumbnailator.Thumbnails.of(sourcePath).size(size, size).toFile(destinationPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
