package nl.jessevogel.photomanager;

public class Main {

    public static void main(String[] args) {
        // Create controller object
        Controller controller = new Controller();

        // Read commands
        controller.getCommands().read();
    }

}
