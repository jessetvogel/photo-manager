package nl.jessevogel.photomanager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Thumbnailator {

    private static final int MAX_SIZE_SMALL = 720;

    boolean resizeSmall(String sourcePath, String destinationPath) {
        return resize(sourcePath, destinationPath, MAX_SIZE_SMALL);
    }

    private boolean resize(String sourcePath, String destinationPath, int size) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(sourcePath));
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            if(width > height) {
                if(width > size) {
                    height = height * size / width;
                    width = size;
                }
            }
            else {
                if(height > size) {
                    width = width * size / height;
                    height = size;
                }
            }

            BufferedImage resizedImage = new BufferedImage(width, height, type);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, width, height, null);
            g.dispose();

            ImageIO.write(resizedImage, "jpg", new File(destinationPath));
            return true;

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
