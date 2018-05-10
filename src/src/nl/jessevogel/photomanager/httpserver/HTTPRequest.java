package nl.jessevogel.photomanager.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPRequest {

    private static final Pattern patternRequestLine = Pattern.compile("^(GET|POST) (.*) (HTTP\\/1.1)$");
    private static final Pattern patternHeader = Pattern.compile("^([\\w\\-]+): (.*)$");
    private static final Pattern patternURI = Pattern.compile("^(\\/.*?)(?:\\?([^=]+=[^=]+(?:&[^=]+=[^=]+)*))?$");

    private String method;
    private String URI;
    private String URIPath;
    private Map<String, String> queries; // TODO: is it called 'query'?
    private String version;
    private Map<String, String> headers;

    public String getURIPath() {
        return URIPath;
    }

    public String getQuery(String key) {
        if(queries == null) return null;
        return queries.get(key);
    }

    boolean parse(InputStream inputStream) {
        try {
            // Parse request line
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String requestLine = br.readLine();
            Matcher m = patternRequestLine.matcher(requestLine);
            if (!m.find()) return false;
            method = m.group(1);
            URI = m.group(2);
            version = m.group(3);

            // Parse URI
            m = patternURI.matcher(URI);
            if (!m.find()) return false;
            URIPath = m.group(1);

            // Parse queries
            if (m.group(2) != null) {
                queries = new HashMap<>();
                String[] queries = m.group(2).split("&");
                for (String query : queries) {
                    String key = query.substring(0, query.indexOf('='));
                    String value = query.substring(key.length() + 1);
                    this.queries.put(key, value);
                }
            }

            // Parse headers
            headers = new HashMap<>();
            String headerLine;
            while ((headerLine = br.readLine()).length() > 0) {
                m = patternHeader.matcher(headerLine);
                if (!m.find()) return false;
                headers.put(m.group(1), m.group(2));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
