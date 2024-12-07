package application;

import application.mydatabase.Database;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        String reponse;
        Database db = new Database();
        reponse = db.startup();

        if (reponse != null) {
            System.out.println(reponse);
        } else {
            simulate(db);
        }

        System.out.println("\nEnd of processing\n");
    }

    private static void simulate(Database db) {

        Scanner consoleIn = new Scanner(System.in);// Scanner that takes input from console
        System.out.println();// Getting on a new line
        System.out.println("Welcome to Winnipeg Road Transport Database");// label

        String cmd = nextNonEmptyLine(consoleIn, "To get started, ENTER 'm' for Menu: ");
        String output;

        boolean cont = true;

        while (cont) {
            output = processCommand(db, cmd);
            System.out.println(output);
            cmd = nextNonEmptyLine(consoleIn, "Choice >> ");
            cont = cmd != null && !cmd.equalsIgnoreCase("e");
        }

        System.out.println("\nExiting Store Management interface. Have a great day!\n");
        consoleIn.close();

    }

    
    private static String processDatabase(Database db) {
        System.out.println("Initializing the Database, this might take about 4-5 minutes");
        System.out.println(
                "------------------------------------------------------------------------------");
        String response = db.initializeDatabase();
        if (response == null) {
            response = "Database successfully initialized";
        }
        return response;
    }

    private static String processDropDB(Database db) {
        String response;
        System.out.println("Droping the Database, the queries will not work untill database is not initialized again");
        System.out.println(
                "------------------------------------------------------------------------------");

        if ((response = db.dropAllTables()) == null) {
            response = "Database Dropped successfully";
        }

        return response;
    }

    private static String processCommand(Database db, String cmd) {
        String[] args = cmd.split("\\s+");
        // if (command.indexOf(" ") > 0)
        // arg = command.substring(command.indexOf(" ")).trim();

        if (args[0].equalsIgnoreCase("m")) {
            displayMenu();
            return "";
        }

        else if (args[0].equalsIgnoreCase("i")) {
            return processDatabase(db);
        }

        else if (args[0].equalsIgnoreCase("d")) {
            return processDropDB(db);
        }

        else {
            return "Invalid choice. Enter 'm' for Menu";
        }

    }

    private static void displayMenu() {
        
        System.out.println("\ti - Initialize the database\n");
        System.out.println("\td - Delete the Database\n");
        System.out.println("\tm - Display the Menu.\n");
        System.out.println("\te - Exit the system.");

    }


    /**
     * Helper method for Scanner to skip over empty lines.
     * Print the prompt on each line of input.
     */
    private static String nextNonEmptyLine(Scanner in, String prompt) {
        String line = null;

        System.out.print(prompt);
        while (line == null && in.hasNextLine()) {
            line = in.nextLine();
            if (line.trim().length() == 0) {
                line = null;
                System.out.print(prompt);
            }
        }

        return line;
    }
}

