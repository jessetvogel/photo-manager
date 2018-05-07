package nl.jessevogel.photomanager.data;

import java.util.HashSet;
import java.util.Set;

public class AlbumPictureConnection {

    public Album album;
    public Set<Picture> pictures;

    public AlbumPictureConnection() {
        pictures = new HashSet<>();
    }

}
