package nl.jessevogel.photomanager.data;

public abstract class Item {

    public int id;

    public abstract String serialize();
    public abstract boolean set(String s);

}
