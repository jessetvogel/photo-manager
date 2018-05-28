package nl.jessevogel.jfifmetadata.segments;

import java.io.FileOutputStream;
import java.util.Arrays;

public class APP1Segment extends Segment {

    public String segmentHeader;
    public byte[] segmentData;

    public APP1Segment() {
        markerCode = 0xE1;
    }

    @Override
    public int read(byte[] data, int pointer) throws Exception {
        // Read length
        int length = read2Bytes(data, pointer); pointer += 2;

        // Read null-terminated header string
        int i;
        for(i = pointer;i < data.length;++ i)
            if(data[i] == '\0') break;
        segmentHeader = new String(data, pointer, i - pointer);

        // Read data
        int pointerEnd = i + 1 + length - 2 - segmentHeader.length() - 1;
        segmentData = Arrays.copyOfRange(data, i + 1, pointerEnd);

        return pointerEnd;
    }

    @Override
    public void write(FileOutputStream output) throws Exception {
        // Write length
        write2Bytes(output, 2 + segmentHeader.length() + 1 + segmentData.length);

        // Write null-terminated header string
        writeString(output, segmentHeader);
        output.write('\0');

        // Write data
        output.write(segmentData);
    }

}
