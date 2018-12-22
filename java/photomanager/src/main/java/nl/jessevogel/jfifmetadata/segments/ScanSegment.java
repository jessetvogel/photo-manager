package nl.jessevogel.jfifmetadata.segments;

import java.io.FileOutputStream;
import java.util.Arrays;

public class ScanSegment extends Segment {

    private byte[] data;

    public ScanSegment() {
        markerCode = 0xDA;
    }

    @Override
    public int read(byte[] data, int pointer) throws Exception {
        // Copy data from pointer until 0xFFD9
        int i;
        for(i = pointer;i < data.length - 1; ++ i)
            if((data[i] & 0xFF) == 0xFF && (data[i + 1] & 0xFF) == 0xD9) break;

        this.data = Arrays.copyOfRange(data, pointer, i);
        return i + 2;
    }

    @Override
    public void write(FileOutputStream output) throws Exception {
        // Write compressed image data
        output.write(data);

        // End of scan marker
        output.write(0xFF);
        output.write(0xD9);
    }

}
