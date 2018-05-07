package nl.jessevogel.photomanager.data;

public class Picture {

    public int id;
    public int albumId;
    public String filename;

    public String serialize() {
        return "" + id + "," + albumId + "," + filename;
    }

    public boolean set(String value) {
        try {
            String[] parts = value.split("(?<!\\\\),");
            if (parts.length != 3) return false;
            this.id = Integer.parseInt(parts[0]);
            this.albumId = Integer.parseInt(parts[1]);
            this.filename = parts[2];
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
