package nl.jessevogel.jfifmetadata.segments;

import java.io.FileOutputStream;
import java.io.IOException;

public abstract class Segment {

    protected int markerCode;

    public abstract int read(byte[] data, int pointer) throws Exception;
    public abstract void write(FileOutputStream output) throws Exception;

    public int getMarkerCode() {
        return markerCode;
    }

    public int read2Bytes(byte[] data, int pointer) throws IOException {
        return 256 * (data[pointer] & 0xFF) + (data[pointer + 1] & 0xFF);
    }

    public void write2Bytes(FileOutputStream output, int n) throws IOException {
        output.write((n / 256) % 256);
        output.write(n % 256);
    }

    public void writeString(FileOutputStream output, String string) throws IOException {
        output.write(string.getBytes());
    }

}
