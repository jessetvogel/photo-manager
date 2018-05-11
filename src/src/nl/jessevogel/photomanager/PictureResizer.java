package nl.jessevogel.photomanager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PictureResizer {

    public boolean resize(String sourcePath, String destinationPath) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(sourcePath));
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

            int width = 100;
            int height = 100;

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
