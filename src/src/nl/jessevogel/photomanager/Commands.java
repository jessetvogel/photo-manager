package nl.jessevogel.photomanager;

import nl.jessevogel.photomanager.data.Album;
import nl.jessevogel.photomanager.data.Person;
import nl.jessevogel.photomanager.data.Picture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        if (command.matches("\\s*")) return true;
        String[] parts = command.split("\\s+");

        if (parts[0].equals("help") && parts.length == 1) return commandHelp();
        if (parts[0].equals("data") && parts.length == 2) return commandData(parts[1]);
        if (parts[0].equals("set") && parts.length == 3) return commandSet(parts[1], parts[2]);
        if (parts[0].equals("get") && parts.length == 2) return commandGet(parts[1]);
        if (parts[0].equals("scan") && parts.length == 1) return commandScan();

        // If no command found, return false
        return false;
    }

    private boolean commandScan() {
        Scanner scanner = new Scanner(controller);
        return scanner.scan() && controller.getData().storeData();
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

    private boolean commandData(String subject) {
        if (subject.equals("albums")) {
            for (Album album : controller.getData().getAlbums())
                System.out.println("[ALBUM] " + album.title + ", id = " + album.id + ", path = " + album.path);
            return true;
        }

        if (subject.equals("people")) {
            for (Person person : controller.getData().getPeople())
                System.out.println("[PERSON] " + person.name + ", id = " + person.id);
            return true;
        }

        if (subject.equals("pictures")) {
            for (Picture picture : controller.getData().getPictures())
                System.out.println("[PICTURE] " + picture.filename + ", id = " + picture.id + ", albumId = " + picture.albumId);
            return true;
        }

        return false;
    }

    private boolean commandGet(String key) {
        if (key.equals("root_directory")) {
            System.out.println(controller.getData().getRootDirectory());
            return true;
        }

        return false;
    }

    private boolean commandSet(String key, String value) {
        if (key.equals("root_directory")) {
            if (!controller.getData().setRootDirectory(value)) {
                System.out.println("Failed to set root directory to '" + value + "'");
                return false;
            }
            return true;
        }

        return false;
    }

}
