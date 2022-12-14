package nl.jessevogel.photomanager;

import nl.jessevogel.jfifmetadata.JFIFImage;
import nl.jessevogel.jfifmetadata.JFIFReader;
import nl.jessevogel.jfifmetadata.JFIFWriter;
import nl.jessevogel.jfifmetadata.segments.APP1Segment;
import nl.jessevogel.jfifmetadata.segments.Segment;
import nl.jessevogel.photomanager.data.Person;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class Tagger {

    private static final String APPLICATION_SEGMENT_HEADER = "photomanager"; // TODO: good name?

    boolean tagPeople(String path, ArrayList<Person> peopleToTag) {
        return tagPeople(path, peopleToTag, null);
    }

    boolean untagPeople(String path, ArrayList<Person> peopleToUntag) {
        return tagPeople(path, null, peopleToUntag);
    }

    private boolean tagPeople(String path, ArrayList<Person> peopleToTag, ArrayList<Person> peopleToUntag) {
        if (peopleToTag == null && peopleToUntag == null) return true;

        try {
            // Parse image and get application segment
            JFIFReader reader = new JFIFReader();
            JFIFImage image = reader.read(path);
            APP1Segment app1Segment = getApplicationSegment(image);

            // Parse JSON
            JSONObject jsonObject = getJSONFromApplicationSegment(app1Segment);
            JSONArray jsonPeopleArray;
            if (jsonObject.has("people")) {
                jsonPeopleArray = jsonObject.getJSONArray("people");
            } else {
                jsonPeopleArray = new JSONArray();
                jsonObject.put("people", jsonPeopleArray);
            }

            // Add the names that are not yet in the array
            if (peopleToTag != null) {
                for (Person person : peopleToTag) {
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
            }

            // Preserve the names that are not in the array list
            if (peopleToUntag != null) {
                JSONArray jsonPeopleArrayUpdated = new JSONArray();
                int n = jsonPeopleArray.length();
                for (int i = 0; i < n; ++i) {
                    String name = jsonPeopleArray.getString(i);
                    boolean found = false;
                    for (Person person : peopleToUntag) {
                        if (person.name.equals(name)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) jsonPeopleArrayUpdated.put(name);
                }
                jsonObject.put("people", jsonPeopleArrayUpdated);
            }

            // Update segment and break out of loop
            storeJSONInApplicationSegment(app1Segment, jsonObject);

            // Update image
            JFIFWriter writer = new JFIFWriter();
            writer.write(image, path);
            return true;
        } catch (Exception e) {
            Log.error("Failed to tag people in " + path);
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

        // If it does not exist, create and insert it (right before the last segment which is the '0xFFDA -- 0xFFD9' part)
        APP1Segment app1Segment = new APP1Segment();
        app1Segment.segmentHeader = APPLICATION_SEGMENT_HEADER;
        app1Segment.segmentData = new byte[0];
        image.addSegment(image.amountOfSegments() - 1, app1Segment);
        return app1Segment;
    }

    private JSONObject getJSONFromApplicationSegment(APP1Segment app1Segment) {
        JSONObject jsonObject;
        try {
            // Try to parse JSON object from segment data
            jsonObject = new JSONObject(new String(app1Segment.segmentData, StandardCharsets.UTF_8));
        } catch (Exception e) {
            // If it fails, create a new JSON object
            jsonObject = new JSONObject();
        }

        return jsonObject;
    }

    private void storeJSONInApplicationSegment(APP1Segment app1Segment, JSONObject jsonObject) {
        app1Segment.segmentData = jsonObject.toString().getBytes();
    }

}
