package nl.jessevogel.jfifmetadata;

import nl.jessevogel.jfifmetadata.segments.APP1Segment;
import nl.jessevogel.jfifmetadata.segments.GeneralSegment;
import nl.jessevogel.jfifmetadata.segments.ScanSegment;
import nl.jessevogel.jfifmetadata.segments.Segment;

import java.nio.file.Files;
import java.nio.file.Paths;

public class JFIFReader {

    public JFIFImage read(String path) throws Exception {
        // Open file for input and read segments
        byte[] data = Files.readAllBytes(Paths.get(path));
        int pointer = 0;
        JFIFImage image = new JFIFImage();
        while ((pointer = readSegment(data, pointer, image)) < data.length) ;
        return image;
    }

    public JFIFImage readMetaData(String path) throws Exception {
        // Open file for input and read segments
        byte[] data = Files.readAllBytes(Paths.get(path));
        int pointer = 0;
        JFIFImage image = new JFIFImage();
        while ((pointer = readMetadataSegment(data, pointer, image)) < data.length) ;
        return image;
    }

    private int readSegment(byte[] data, int pointer, JFIFImage image) throws Exception {
        // Markers should be of the form '0xFF 0x..'
        int FF = data[pointer++] & 0xFF;
        if (FF != 0xFF) throw new Exception("Invalid JFIF file");
        int markerCode = data[pointer++] & 0xFF;

        // Depending on marker type, create segments
        Segment segment;
        switch (markerCode) {

            case 0xE1:
                segment = new APP1Segment();
                pointer = segment.read(data, pointer);
                image.addSegment(segment);
                break;

            case 0xDA:
                segment = new ScanSegment();
                pointer = segment.read(data, pointer);
                image.addSegment(segment);
                break;

            case 0xD0:
            case 0xD1:
            case 0xD2:
            case 0xD3:
            case 0xD4:
            case 0xD5:
            case 0xD6:
            case 0xD7:
            case 0xD8:
            case 0xD9:
                segment = new GeneralSegment(markerCode);
                image.addSegment(segment);
                break;

            default:
                segment = new GeneralSegment(markerCode);
                pointer = segment.read(data, pointer);
                image.addSegment(segment);
                break;
        }

        return pointer;
    }

    private int readMetadataSegment(byte[] data, int pointer, JFIFImage image) throws Exception {
        // Markers should be of the form '0xFF 0x..'
        int FF = data[pointer++] & 0xFF;
        if (FF != 0xFF) throw new Exception("Invalid JFIF file");
        int markerCode = data[pointer++] & 0xFF;

        // Only parse if 0xE1 segment
        Segment segment;
        switch (markerCode) {

            case 0xE1:
                segment = new APP1Segment();
                pointer = segment.read(data, pointer);
                image.addSegment(segment);
                break;

            case 0xDA:
                for (; pointer < data.length - 1; ++pointer)
                    if ((data[pointer] & 0xFF) == 0xFF && (data[pointer + 1] & 0xFF) == 0xD9)
                        break;
                pointer += 2;
                break;

            case 0xD0:
            case 0xD1:
            case 0xD2:
            case 0xD3:
            case 0xD4:
            case 0xD5:
            case 0xD6:
            case 0xD7:
            case 0xD8:
            case 0xD9:
                break;

            default:
                int length = 256 * (data[pointer] & 0xFF) + (data[pointer + 1] & 0xFF);
                pointer += length;
                break;
        }

        return pointer;
    }

}
