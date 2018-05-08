package nl.jessevogel.photomanager;

class Controller {

    private static final int API_PORT = 4321;

    private Commands commands;
    private Data data;
    private APIServer apiServer;

    Controller() {
        // Create subcontrollers
        commands = new Commands(this);
        data = new Data(this);
        apiServer = new APIServer(this, API_PORT);
    }

    Commands getCommands() {
        return commands;
    }

    Data getData() {
        return data;
    }

    APIServer getAPIServer() {
        return apiServer;
    }
}
