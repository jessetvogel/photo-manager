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

        if (parts[0].equals("help")) return commandHelp();
        if (parts[0].equals("data")) return commandData(parts);
        if (parts[0].equals("set")) return commandSet(parts);
        if (parts[0].equals("get")) return commandGet(parts);
        if (parts[0].equals("scan")) return commandScan();
        if (parts[0].equals("wipe")) return commandWipe();

        // If no command found, return false
        return false;
    }

    private boolean commandWipe() {
        controller.getData().clear();
        // TODO: clear /.data directory (?)
        return true;
    }

    private boolean commandScan() {
        Scanner scanner = new Scanner(controller);
        if (scanner.scan()) {
            if(!controller.getData().storeData())
                Log.error("Failed to store data!");
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
            for (Album album : controller.getData().getAlbums())
                System.out.println("[ALBUM] " + album.title + ", id = " + album.id + ", path = " + album.path);
            return true;
        }

        if (parts[1].equals("people")) {
            for (Person person : controller.getData().getPeople())
                System.out.println("[PERSON] " + person.name + ", id = " + person.id);
            return true;
        }

        if (parts[1].equals("pictures")) {
            for (Picture picture : controller.getData().getPictures())
                System.out.println("[PICTURE] " + picture.filename + ", id = " + picture.id + ", albumId = " + picture.albumId);
            return true;
        }

        if (parts.length < 3) return false;

        if (parts[1].equals("person")) {
            Person person = controller.getData().getPersonById(Integer.parseInt(parts[2])); // TODO : check if number..
            if (person == null) {
                System.out.println("No person exists with id " + parts[2]);
                return true;
            }

            System.out.println("id: " + person.id);
            System.out.println("name: " + person.name);
            System.out.println("amount of pictures: " + person.pictures.size());
            return true;
        }

        if (parts[1].equals("album")) {
            Album album = controller.getData().getAlbumById(Integer.parseInt(parts[2])); // TODO : check if number..
            if (album == null) {
                System.out.println("No album exists with id " + parts[2]);
                return true;
            }

            System.out.println("id: " + album.id);
            System.out.println("title: " + album.title);
            System.out.println("amount of pictures: " + album.pictures.size());
            return true;
        }

        if (parts[1].equals("picture")) {
            Picture picture = controller.getData().getPictureById(Integer.parseInt(parts[2])); // TODO : check if number..
            if (picture == null) {
                System.out.println("No picture exists with id " + parts[2]);
                return true;
            }

            System.out.println("id: " + picture.id);
            System.out.println("filename: " + picture.filename);
            System.out.println("album: " + controller.getData().getAlbumById(picture.albumId).title);
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

}
