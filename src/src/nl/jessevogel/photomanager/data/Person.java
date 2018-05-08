package nl.jessevogel.photomanager.data;

import java.util.HashSet;
import java.util.Set;

public class Person {

    public int id;
    public String name;
    public Set<Picture> pictures;

    public Person() {
        pictures = new HashSet<>();
    }

    public String serialize() {
        return "" + id + "," + name;
    }

    public boolean set(String value) {
        try {
            String[] parts = value.split("(?<!\\\\),");
            if (parts.length != 2) return false;
            this.id = Integer.parseInt(parts[0]);
            this.name = parts[1];
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
