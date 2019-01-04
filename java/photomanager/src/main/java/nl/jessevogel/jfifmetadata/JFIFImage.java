package nl.jessevogel.jfifmetadata;

import nl.jessevogel.jfifmetadata.segments.APP1Segment;
import nl.jessevogel.jfifmetadata.segments.Segment;

import java.util.ArrayList;

public class JFIFImage {

    private ArrayList<Segment> segments;

    public JFIFImage() {
        segments = new ArrayList<>();
    }

    public void addSegment(Segment segment) {
        segments.add(segment);
    }

    public void addSegment(int index, Segment segment) {
        segments.add(index, segment);
    }

    public int amountOfSegments() {
        return segments.size();
    }

    public ArrayList<Segment> getSegments() {
        return segments;
    }

}
