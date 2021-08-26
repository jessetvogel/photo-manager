package nl.jessevogel.photomanager;

class Controller {

    private static final int API_PORT = 4321;
    private static final int WEB_PORT = 8080;

    private final Commands commands;
    private final Data data;
    private final APIServer apiServer;
    private final WebServer webServer;

    Controller() {
        // Create sub-controllers
        commands = new Commands(this);
        data = new Data();
        apiServer = new APIServer(this, API_PORT);
        webServer = new WebServer(WEB_PORT);
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
