package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Person;
import nl.jessevogel.photomanager.data.Picture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

class Commands {

    private final static String PS1 = "> ";

    private Controller controller;

    Commands(Controller controller) {
        this.controller = controller;
    }

    void read() {
        try {
            // Read commands from stdin
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print(PS1);
                String line = br.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.equals("exit")) break;
                if (!parse(line))
                    System.out.println("Unable to parse command '" + line + "'");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean parse(String command) {
        if (command.matches("\\s*(#.*)?")) return true;
        String[] parts = command.split("\\s+");

        if (parts[0].equals("help")) return commandHelp();
        if (parts[0].equals("data")) return commandData(parts);
        if (parts[0].equals("loaddata")) return commandLoadData();
        if (parts[0].equals("storedata")) return commandStoreData();
        if (parts[0].equals("set")) return commandSet(parts);
        if (parts[0].equals("get")) return commandGet(parts);
        if (parts[0].equals("scan")) return commandScan();
        if (parts[0].equals("wipe")) return commandWipe();
        if (parts[0].equals("client")) return commandClient();

        // If no command found, return false
        return false;
    }

    private boolean commandStoreData() {
        if (!controller.getData().storeData())
            System.out.println("Failed to store data!");
        return true;
    }

    private boolean commandLoadData() {
        if (!controller.getData().loadData())
            System.out.println("Failed to load data!");
        return true;
    }

    private boolean commandClient() {
        try {
            String url = "http://localhost:" + controller.getWebServer().getPort();
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("start " + url);
                return true;
            }

            if (os.contains("mac")) {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("open " + url);
                return true;
            }

//            // TODO: implement for Linux/Unix
//            if(os.contains("nix") || os.contains("nux")) {
//                Runtime runtime = Runtime.getRuntime();
//                runtime.exec( ??? );
//                return true;
//            }

            System.out.println("Command 'client' is only implemented for Windows and macOS.");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean commandWipe() {
        controller.getData().clear();
        // TODO: clear /.data directory (?)
        return true;
    }

    private boolean commandScan() {
        Scanner scanner = new Scanner(controller);
        if (scanner.scan()) {
            if (!controller.getData().storeData())
                Log.error("Failed to store data!");
        } else {
            System.out.println("Failed to scan!");
        }
        return true;
    }

    private boolean commandHelp() {
        System.out.println("Commands:");
        System.out.println("  help                        - show list of commands");
        System.out.println();
        System.out.println("  data albums                 - show list of albums");
        System.out.println("  data people                 - show list of people");
        System.out.println("  data pictures               - show list of pictures");
        System.out.println();
        System.out.println("  get root_directory          - get root directory");
        System.out.println("  set root_directory <path>   - set root directory");

        return true;
    }

    private boolean commandData(String[] parts) {
        if (parts.length < 2) return false;

        if (parts[1].equals("albums")) {
            Table table = new Table(4);
            table.add("id");
            table.add("title");
            table.add("path");
            table.add("amount of pictures");
            for (Album album : controller.getData().getAlbums()) {
                table.add("" + album.id);
                table.add(album.title);
                table.add(album.path);
                table.add("" + album.pictures.size());
            }
            table.print();
            return true;
        }

        if (parts[1].equals("people")) {
            Table table = new Table(3);
            table.add("id");
            table.add("name");
            table.add("amount of pictures");
            for (Person person : controller.getData().getPeople()) {
                table.add("" + person.id);
                table.add(person.name);
                table.add("" + person.pictures.size());
            }
            table.print();
            return true;
        }

        if (parts[1].equals("pictures")) {
            Table table = new Table(3);
            table.add("id");
            table.add("filename");
            table.add("album id");
            for (Picture picture : controller.getData().getPictures()) {
                table.add("" + picture.id);
                table.add(picture.filename);
                table.add("" + picture.albumId);
            }
            table.print();
            return true;
        }

        return false;
    }

    private boolean commandGet(String[] parts) {
        if (parts.length != 2) return false;

        if (parts[1].equals("root_directory")) {
            System.out.println(controller.getData().getRootDirectory());
            return true;
        }

        return false;
    }

    private boolean commandSet(String[] parts) {
        if (parts.length != 3) return false;

        if (parts[1].equals("root_directory")) {
            if (!controller.getData().setRootDirectory(parts[2])) {
                System.out.println("Failed to set root directory to '" + parts[2] + "'");
                return false;
            }
            return true;
        }

        return false;
    }

    void executeFile(String path) {
        DataFile dataFile = new DataFile(path);
        if (!dataFile.exists()) {
            System.out.println("Could not find file " + path);
            return;
        }
        String line;
        while ((line = dataFile.readLine()) != null) {
            if (!parse(line))
                System.out.println("Unable to parse command '" + line + "'");
        }
        dataFile.close();
    }

    private class Table {

        private int[] maxWidth;
        private int columns;
        private ArrayList<String> entries;

        Table(int columns) {
            this.columns = columns;
            entries = new ArrayList<>();
            maxWidth = new int[columns];
        }

        void add(String entry) {
            int i = entries.size();
            maxWidth[i % columns] = Math.max(maxWidth[i % columns], entry.length());
            entries.add(entry);
        }

        void print() {
            int N = entries.size();
            for (int i = 0; i < N; ++i) {
                String entry = entries.get(i);
                System.out.print(entry);
                System.out.print(new String(new char[maxWidth[i % columns] - entry.length() + 2]).replace('\0', ' '));
                if ((i + 1) % columns == 0) System.out.println();
            }
            System.out.println();
        }

    }

}
