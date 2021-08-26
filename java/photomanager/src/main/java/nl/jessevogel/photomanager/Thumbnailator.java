package nl.jessevogel.photomanager;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Thumbnailator {

    private static final int MAX_SIZE_SMALL = 720;

    boolean thumbnailPhoto(String sourcePath, String destinationPath) {
        try {
            net.coobird.thumbnailator.Thumbnails.of(sourcePath).size(MAX_SIZE_SMALL, MAX_SIZE_SMALL).toFile(destinationPath);
            return true;
        } catch (IOException e) {
            Log.error("Failed to resize file " + sourcePath);
            return false;
        }
    }

    boolean thumbnailVideo(String sourcePath, String destinationPath) {
        try {
            FFmpegFrameGrabber g = new FFmpegFrameGrabber(sourcePath);
            g.start();
            ImageIO.write(
                    downscaleBufferedImage(new Java2DFrameConverter().getBufferedImage(g.grabKeyFrame())),
                    "jpg",
                    new File(destinationPath)
            );
            g.stop();
            return true;
        } catch (IOException e) {
            Log.error(e.getMessage());
            return false;
        }
    }

    public static BufferedImage downscaleBufferedImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        if (w < MAX_SIZE_SMALL && h < MAX_SIZE_SMALL)
            return image; // No scaling needs to be done!

        // Create scaled image
        int width = (w > h) ? MAX_SIZE_SMALL : MAX_SIZE_SMALL * w / h;
        int height = (w > h) ? MAX_SIZE_SMALL * h / w : MAX_SIZE_SMALL;
        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        // Create a buffered image with RGB channels, and draw scaledImage to bi
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.drawImage(scaledImage, 0, 0, null);
        graphics2D.dispose();
        return bi;
    }
}
