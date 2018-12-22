package nl.jessevogel.jfifmetadata;

import nl.jessevogel.jfifmetadata.segments.Segment;

import java.io.FileOutputStream;

public class JFIFWriter {

    public void write(JFIFImage image, String path) throws Exception {
        // Open file and write all segments to it
        FileOutputStream output = new FileOutputStream(path);
        for(Segment segment : image.getSegments()) {
            // Write marker
            output.write(0xFF);
            output.write(segment.getMarkerCode());
            segment.write(output);
        }
        output.close();
    }

}
