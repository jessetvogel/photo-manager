package nl.jessevogel.jfifmetadata.segments;

import java.io.FileOutputStream;
import java.util.Arrays;

public class GeneralSegment extends Segment {

    public byte data[] = null;

    public GeneralSegment(int markerCode) {
        this.markerCode = markerCode;
    }

    @Override
    public int read(byte[] data, int pointer) throws Exception {
        int length = read2Bytes(data, pointer); pointer += 2;
        this.data = Arrays.copyOfRange(data, pointer, pointer + length - 2);
        return pointer + length - 2;
    }

    @Override
    public void write(FileOutputStream output) throws Exception {
        if(data != null) {
            write2Bytes(output, data.length + 2);
            output.write(data);
        }
    }

}
