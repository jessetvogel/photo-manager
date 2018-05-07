package nl.jessevogel.photomanager.data;

import java.util.HashSet;
import java.util.Set;

public class PersonPictureConnection {

    public Person person;
    public Set<Picture> pictures;

    public PersonPictureConnection() {
        pictures = new HashSet<>();
    }

}
