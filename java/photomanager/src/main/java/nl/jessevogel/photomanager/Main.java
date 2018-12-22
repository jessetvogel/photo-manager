package nl.jessevogel.photomanager;

public class Main {

    public static void main(String[] args) {
        // Create controller object
        Controller controller = new Controller();

        // Start API & Web server
        controller.getAPIServer().start();
        controller.getWebServer().start();

        // Read settings file
        controller.getCommands().executeFile("photomanager.conf");

        // Read commands
        controller.getCommands().read();

        // Stop API & Web server
        controller.getAPIServer().stop();
        controller.getWebServer().stop();

        // Store data TODO: this should not be here ?
        controller.getData().storeData();
    }

}
