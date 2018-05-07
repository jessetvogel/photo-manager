package nl.jessevogel.photomanager;

class Controller {

    private Commands commands;
    private Data data;

    Controller() {
        commands = new Commands(this);
        data = new Data(this);
        data.loadData();
    }

    Commands getCommands() {
        return commands;
    }

    Data getData() {
        return data;
    }

}
