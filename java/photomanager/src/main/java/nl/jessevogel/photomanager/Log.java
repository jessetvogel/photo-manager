package nl.jessevogel.photomanager;

public class Log {

    static void error(String message) {
        System.err.println("[ERROR] " + message);
    }

    static void debug(String message) {
        System.out.println("[DEBUG] " + message);
    }

    static void print(String message) {
        System.out.print(message);
        System.out.flush();
    }

    static void println(String message) {
        System.out.println(message);
    }

    public static void warning(String message) {
        System.out.println("[WARNING] " + message);
    }

    static void updatePercentage(int percentage) {
        System.out.print("\b\b\b\b");
        if (percentage < 10) System.out.print(" ");
        if (percentage < 100) System.out.print(" ");
        System.out.print("" + percentage + "%");
        System.out.flush();
    }
}
