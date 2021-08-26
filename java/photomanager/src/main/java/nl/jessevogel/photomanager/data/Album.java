package nl.jessevogel.photomanager.data;

import java.util.HashSet;
import java.util.Set;

public class Album extends Item {

    public String title;
    public String path;
    public Set<Medium> media;

    public Album() {
        media = new HashSet<>();
    }

    @Override
    public String serialize() {
        return "" + id + "," + title + "," + path;
    }

    @Override
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
