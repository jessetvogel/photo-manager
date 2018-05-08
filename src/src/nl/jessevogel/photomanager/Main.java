package nl.jessevogel.photomanager;

public class Main {

    public static void main(String[] args) {
        // Create controller object
        Controller controller = new Controller();

        // Load data
        controller.getData().loadData();

        // Start API server
        controller.getAPIServer().start();

        // Read commands
        controller.getCommands().read();

        // Stop API server
        controller.getAPIServer().stop();
    }

}
