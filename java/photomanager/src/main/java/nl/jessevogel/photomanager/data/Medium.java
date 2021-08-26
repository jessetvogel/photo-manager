package nl.jessevogel.photomanager.data;

import java.util.Map;

public class Medium extends Item {

    public enum Type { PHOTO, VIDEO }

    public static final Map<String, Type> extensionToType = Map.of(
            "jpg", Type.PHOTO,
            "jpeg", Type.PHOTO,
            "mp4", Type.VIDEO
    );

    public int albumId;
    public String filename;
    public Type type;

    @Override
    public String serialize() {
        return "" + id + "," + albumId + "," + filename;
    }

    @Override
    public boolean set(String value) {
        try {
            String[] parts = value.split("(?<!\\\\),");
            if (parts.length != 3) return false;
            id = Integer.parseInt(parts[0]);
            albumId = Integer.parseInt(parts[1]);
            filename = parts[2];
            type = typeFromExtension(getFileExtension(filename));
            return type != null;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String getFileExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i + 1).toLowerCase() : "";
    }

    public static Type typeFromExtension(String extension) {
        return extensionToType.get(extension);
    }

}
