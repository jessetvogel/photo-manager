package nl.jessevogel.photomanager;

class Controller {

    private static final int API_PORT = 4321;
    private static final int WEB_PORT = 8080;

    private Commands commands;
    private Data data;
    private APIServer apiServer;
    private WebServer webServer;

    Controller() {
        // Create sub-controllers
        commands = new Commands(this);
        data = new Data();
        apiServer = new APIServer(this, API_PORT);
        webServer = new WebServer(this, WEB_PORT);
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

    WebServer getWebServer() {
        return webServer;
    }

}
