package nl.jessevogel.jfifmetadata.segments;

import java.io.FileOutputStream;

public class APP0Segment extends Segment {

    public JFIFData jfifData = null;
    public JFXXData jfxxData = null;

    public APP0Segment() {
        markerCode = 0xE0;
    }

    @Override
    public int read(byte[] data, int pointer) throws Exception {
//        int length = read2Bytes(input);
//        String identifier = readString(input, 5);
//
//        if (identifier.equals("JFIF\0")) {
//            jfifData = new JFIFData();
//            jfifData.versionMajor = input.read();
//            jfifData.versionMinor = input.read();
//            switch (input.read()) {
//                case 0x00:
//                    jfifData.densityUnits = JFIFDensityUnits.NO_UNITS;
//                    break;
//                case 0x01:
//                    jfifData.densityUnits = JFIFDensityUnits.PIXELS_PER_INCH;
//                    break;
//                case 0x02:
//                    jfifData.densityUnits = JFIFDensityUnits.PIXELS_PER_CENTIMETER;
//                    break;
//                default:
//                    throw new Exception("Invalid value density units");
//            }
//
//            jfifData.xDensity = read2Bytes(input);
//            if (jfifData.xDensity == 0) throw new Exception("xDensity may not be zero");
//            jfifData.yDensity = read2Bytes(input);
//            if (jfifData.yDensity == 0) throw new Exception("yDensity may not be zero");
//
//            jfifData.xThumbnail = input.read();
//            jfifData.yThumbnail = input.read();
//            int thumbnailDataLength = 3 * jfifData.xThumbnail * jfifData.yThumbnail;
//            jfifData.thumbnailData = new byte[thumbnailDataLength];
//            input.read(jfifData.thumbnailData); // TODO: use result
//
//            if (length != 16 + thumbnailDataLength) throw new Exception("Length did not match actual length (in APP0)");
//            return;
//        }
//
//        if (identifier.equals("JFXX\0")) {
//            jfxxData = new JFXXData();
//            switch (input.read()) {
//                case 0x10: jfxxData.thumbnailFormat = JFXXThumbnailFormat.JPEG_FORMAT; break;
//                case 0x11: jfxxData.thumbnailFormat = JFXXThumbnailFormat.PALETTIZED_FORMAT; break;
//                case 0x13: jfxxData.thumbnailFormat = JFXXThumbnailFormat.RGB_FORMAT; break;
//                default: throw new Exception("Invalid thumbnail format density units");
//            }
//
//            jfxxData.thumbnailData = new byte[length - 8];
//            input.read(jfxxData.thumbnailData); // TODO: maybe validate this data sometime?
//            return;
//        }
//
//        throw new Exception("Unknown identifier (in APP0)");
        return 0;
    }

    @Override
    public void write(FileOutputStream input) {
        // TODO: implement
    }

    enum JFIFDensityUnits { NO_UNITS, PIXELS_PER_INCH, PIXELS_PER_CENTIMETER }
    enum JFXXThumbnailFormat { JPEG_FORMAT, PALETTIZED_FORMAT, RGB_FORMAT }

    class JFIFData {
        public int versionMajor;
        public int versionMinor;
        public JFIFDensityUnits densityUnits;
        public int xDensity;
        public int yDensity;
        public int xThumbnail;
        public int yThumbnail;
        public byte[] thumbnailData;
    }

    class JFXXData {
        public JFXXThumbnailFormat thumbnailFormat;
        public byte[] thumbnailData;
    }

}