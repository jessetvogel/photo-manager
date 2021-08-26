package nl.jessevogel.photomanager;

import java.io.*;

class DataFile {

    enum Type {Reading, Writing, Closed}

    private final String path;
    private Type type;

    private PrintWriter writer;
    private BufferedReader reader;
    private int lineNumber;

    DataFile(String path) {
        this.path = path;
        type = Type.Closed;
    }

    void touch() {
        try {
            File file = new File(path);
            if (file.exists() && !file.isDirectory()) return;
            if (file.getParentFile().mkdirs()) file.createNewFile();
        } catch (IOException e) {
            Log.error("Failed to touch file " + path);
        }
    }

    void touchWrite() {
        touch();
        write();
    }

    boolean exists() {
        File file = new File(path);
        return file.exists() && !file.isDirectory();
    }

    String readLine() {
        // If not reading nor closed, can't read
        if (type != Type.Reading && type != Type.Closed) return null;

        // If closed, open for reading
        if (type == Type.Closed) {
            try {
                reader = new BufferedReader(new FileReader(path));
                type = Type.Reading;
                lineNumber = 0;
            } catch (IOException e) {
                Log.error("Failed to open file for reading " + path);
                return null;
            }
        }

        // Read line
        try {
            lineNumber++;
            return reader.readLine();
        } catch (IOException e) {
            Log.error("Failed to read from file " + path);
            return null;
        }
    }

    private boolean write() {
        try {
            writer = new PrintWriter(path);
            type = Type.Writing;
            lineNumber = 0;
            return true;
        } catch (IOException e) {
            Log.error("Failed to open file for writing " + path);
            return false;
        }
    }

    boolean writeLine(String line) {
        // If not writing nor closed, can't read
        if (type != Type.Writing && type != Type.Closed)
            return false;

        // If closed, open for writing
        if (type == Type.Closed && !write())
            return false;

        // Write line
        lineNumber++;
        writer.println(line);
        return true;
    }

    int getLineNumber() {
        return lineNumber;
    }

    void close() {
        if (type == Type.Closed)
            return;

        if (type == Type.Reading) {
            try {
                reader.close();
                type = Type.Closed;
            } catch (IOException e) {
                Log.error("Failed to close file " + path);
            }
            return;
        }

        if (type == Type.Writing) {
            writer.close();
            type = Type.Closed;
        }
    }

}
