package nl.jessevogel.photomanager;

import nl.jessevogel.jfifmetadata.JFIFImage;
import nl.jessevogel.jfifmetadata.JFIFReader;
import nl.jessevogel.jfifmetadata.JFIFWriter;
import nl.jessevogel.jfifmetadata.segments.APP1Segment;
import nl.jessevogel.jfifmetadata.segments.Segment;
import nl.jessevogel.photomanager.data.Person;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Tagger {

    private static final String APPLICATION_SEGMENT_HEADER = "photomanager";

    public boolean tagPeople(String path, ArrayList<Person> people) {
        try {
            // Parse image and get application segment
            JFIFReader reader = new JFIFReader();
            JFIFImage image = reader.read(path);
            APP1Segment app1Segment = getApplicationSegment(image);

            // Parse JSON
            JSONObject jsonObject = new JSONObject(new String(app1Segment.segmentData, "UTF-8"));
            JSONArray jsonPeopleArray;
            if (jsonObject.has("people")) {
                jsonPeopleArray = jsonObject.getJSONArray("people");
            } else {
                jsonPeopleArray = new JSONArray();
                jsonObject.put("people", jsonPeopleArray);
            }

            // Only add the names that are not yet in the array
            for (Person person : people) {
                boolean found = false;
                int n = jsonPeopleArray.length();
                for (int i = 0; i < n; ++i) {
                    if (jsonPeopleArray.getString(i).equals(person.name)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    jsonPeopleArray.put(person.name);
            }

            // Update segment and break out of loop
            app1Segment.segmentData = jsonObject.toString().getBytes();

            // Update image
            JFIFWriter writer = new JFIFWriter();
            writer.write(image, path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean untagPeople(String path, ArrayList<Person> people) {
        try {
            // Parse image and get application segment
            JFIFReader reader = new JFIFReader();
            JFIFImage image = reader.read(path);
            APP1Segment app1Segment = getApplicationSegment(image);

            // Parse JSON
            JSONObject jsonObject = new JSONObject(new String(app1Segment.segmentData, "UTF-8"));
            if (!jsonObject.has("people"))
                return true; // If it does not contains a 'people' field, then there is nothing to be done
            JSONArray jsonPeopleArray = jsonObject.getJSONArray("people");

            // Only preserve the names that are not in the arraylist
            JSONArray jsonPeopleArrayUpdated = new JSONArray();
            int n = jsonPeopleArray.length();
            for (int i = 0; i < n; ++i) {
                String name = jsonPeopleArray.getString(i);
                boolean found = false;
                for (Person person : people) {
                    if (person.name.equals(name)) {
                        found = true;
                        break;
                    }
                }
                if (!found) jsonPeopleArrayUpdated.put(name);
            }
            jsonObject.put("people", jsonPeopleArrayUpdated);

            // Update segment and break out of loop
            app1Segment.segmentData = jsonObject.toString().getBytes();

            // Update image
            JFIFWriter writer = new JFIFWriter();
            writer.write(image, path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private APP1Segment getApplicationSegment(JFIFImage image) {
        // Try to find existing application segment
        for (Segment segment : image.getSegments()) {
            // Must have marker code 0xE1 and segment header given by APPLICATION_SEGMENT_HEADER
            if (segment.getMarkerCode() != 0xE1) continue;
            APP1Segment app1Segment = (APP1Segment) segment;
            if (!app1Segment.segmentHeader.equals(APPLICATION_SEGMENT_HEADER)) continue;
            return app1Segment;
        }

        // If it does not exist, create and add it
        APP1Segment app1Segment = new APP1Segment();
        app1Segment.segmentHeader = APPLICATION_SEGMENT_HEADER;
        app1Segment.segmentData = new byte[0];
        return app1Segment;
    }

}
