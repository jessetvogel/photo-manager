package nl.jessevogel.photomanager;

import java.util.HashMap;
import java.util.Map;

public class MIMEType {

    private static MIMEType singleton = null;
    private static final String DEFAULT_TYPE = "text/plain";

    private final Map<String, String> types;

    private MIMEType() {
        types = new HashMap<>();
        // Text files
        types.put("html", "text/html");
        types.put("css", "text/css");
        types.put("txt", "text/plain");

        // Application files
        types.put("js", "application/javascript");

        // Images
        types.put("ico", "image/x-icon");
        types.put("jpg", "image/jpeg");
        types.put("jpeg", "image/jpeg");
        types.put("png", "image/png");

        // Videos
        types.put("mp4", "video/mp4");
    }

    public static String getByExtension(String extension) {
        if (singleton == null) singleton = new MIMEType();
        String type = singleton.types.get(extension);
        if (type != null) return type;
        return DEFAULT_TYPE;
    }

    public static String getByFile(String path) {
        int i = path.lastIndexOf('.');
        if (i == -1) return getByExtension("");
        return getByExtension(path.substring(i + 1));
    }
}
