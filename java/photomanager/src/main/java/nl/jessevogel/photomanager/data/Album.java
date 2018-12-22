package nl.jessevogel.photomanager.data;

import java.util.HashSet;
import java.util.Set;

public class Album {

    public int id;
    public String title;
    public String path;
    public Set<Picture> pictures;

    public Album() {
        pictures = new HashSet<>();
    }
    public String serialize() {
        return "" + id + "," + title + "," + path;
    }

    public boolean set(String value) {
        try {
            String[] parts = value.split("(?<!\\\\),");
            if (parts.length != 3) return false;
            this.id = Integer.parseInt(parts[0]);
            this.title = parts[1];
            this.path = parts[2];
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
