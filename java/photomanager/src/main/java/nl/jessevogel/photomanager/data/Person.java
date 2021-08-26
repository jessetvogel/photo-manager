package nl.jessevogel.photomanager.data;

import java.util.HashSet;
import java.util.Set;

public class Person extends Item {

    public String name;
    public Set<Medium> media;

    public Person() {
        media = new HashSet<>();
    }

    @Override
    public String serialize() {
        return "" + id + "," + name;
    }

    @Override
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
