package application.mydatabase;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Database {
    private Connection connection;
    private static final String regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private final String cfgFilePath = "src/application/auth.cfg";

    public Database() {

    }

    /**
     * This method will establish the connection to the uranium database.
     * If you are not on campus wifi, you must be connect to UofM VPN
     * Driver file according to the java version is requied, auth.cfg file to the
     * root director this project is also required
     *
     * @return null on success, or an erro message if something went wrong
     */
    public String startup() {
        String response = null;

        Properties prop = new Properties();

        try {
            FileInputStream configFile = new FileInputStream(cfgFilePath);
            prop.load(configFile);
            configFile.close();
            final String username = (prop.getProperty("username"));
            final String password = (prop.getProperty("password"));

            String url = "jdbc:sqlserver://uranium.cs.umanitoba.ca:1433;"
                    + "database=cs3380;"
                    + "user=" + username + ";"
                    + "password= " + password + ";"
                    + "encrypt=false;trustServerCertificate=false;loginTimeout=30;";

            this.connection = DriverManager.getConnection(url);

        } catch (FileNotFoundException fnf) {
            response = "\nAn error occurred: config file not found.";
        } catch (IOException io) {
            response = "\nAn error occurred: could not read config file.";
        } catch (SQLException se) {
            se.printStackTrace();
            response = "\nAn error occured: Failed to establish connection to database";
        }

        return response;
    }

    public String initializeDatabase() {
        String response;

        response = dropAllTables();

        if (response == null) {
            response = createAllTables();
        }

        return response;
    }

    private String createAllTables() {
        String response = null;
    
        try {
            System.out.println("Starting table creation...");
    
            System.out.println("Creating Streets table...");
            insertIntoStreet();
            System.out.println("Streets table created successfully.");
    
            System.out.println("Creating Routes table...");
            insertIntoRoutes();
            System.out.println("Routes table created successfully.");
    
            System.out.println("Creating Stops table...");
            insertIntoStop();
            System.out.println("Stops table created successfully.");
    
            System.out.println("Creating SpeedLimits table...");
            insertIntoSpeedLimits();
            System.out.println("SpeedLimits table created successfully.");
    
            System.out.println("Creating CyclingNetwork table...");
            insertIntoCyclingNetwork();
            System.out.println("CyclingNetwork table created successfully.");
    
            System.out.println("Creating TrafficCounts table...");
            insertIntoTrafficCount();
            System.out.println("TrafficCounts table created successfully.");
    
            System.out.println("Creating PassUps table...");
            insertIntoPassUps();
            System.out.println("PassUps table created successfully.");
    
            System.out.println("Creating PassengerActivity table...");
            insertIntoPassengerActivity();
            System.out.println("PassengerActivity table created successfully.");
    
            System.out.println("Creating TransitPerformance table...");
            insertIntoTransitPerformance();
            System.out.println("TransitPerformance table created successfully.");
    
        } catch (SQLException se) {
            se.printStackTrace();
            response = "SQL Exception: " + se.getMessage();
            response += "\nErasing the whole database";
            dropAllTables();
        } catch (IOException io) {
            io.printStackTrace();
            response = "IO Exception: " + io.getMessage();
            response += "\nErasing the whole database";
            dropAllTables();
        }
    
        return response;
    }
    

    private void insertIntoCyclingNetwork() throws SQLException, IOException {
        try {
            // Create the CyclingNetwork table
            this.connection.createStatement().executeUpdate("CREATE TABLE CyclingNetwork ("
                    + "cyclePathID INT IDENTITY(1,1) PRIMARY KEY, " // Use IDENTITY for auto-increment
                    + "startStreetID INT, "
                    + "endStreetID INT, "
                    + "infrastructureType VARCHAR(MAX), " // Use VARCHAR(MAX) instead of TEXT
                    + "infrastructureName VARCHAR(MAX), "
                    + "roadLocation VARCHAR(MAX), "
                    + "twoWayTravel INT, "
                    + "length INT, "
                    + "FOREIGN KEY(startStreetID) REFERENCES Streets(streetID), "
                    + "FOREIGN KEY(endStreetID) REFERENCES Streets(streetID))");
    
            // Reading data from the CSV file
            BufferedReader br = new BufferedReader(new FileReader("final-data-files/Cycling-Network.csv"));
            PreparedStatement pstmt;
            String inputLine;
            String sql;
            String[] inputArr;
    
            br.readLine(); // Skip the headers
    
            while ((inputLine = br.readLine()) != null) {
                inputArr = inputLine.split(regex);
    
                // Prepare SQL statement to insert data
                sql = "INSERT INTO CyclingNetwork (startStreetID, endStreetID, infrastructureType, infrastructureName, roadLocation, twoWayTravel, length) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                pstmt = connection.prepareStatement(sql);
    
                pstmt.setInt(1, Integer.parseInt(inputArr[0])); // startStreetID
                pstmt.setInt(2, Integer.parseInt(inputArr[1])); // endStreetID
                pstmt.setString(3, inputArr[2]); // infrastructureType
                pstmt.setString(4, inputArr[3]); // infrastructureName
                pstmt.setString(5, inputArr[4]); // roadLocation
                pstmt.setInt(6, Integer.parseInt(inputArr[5])); // twoWayTravel
                pstmt.setInt(7, Integer.parseInt(inputArr[6])); // length
    
                pstmt.executeUpdate(); // Execute the insertion
            }
    
            br.close();
            System.out.println("CyclingNetwork table created and populated successfully");
        } catch (IOException io) {
            throw new IOException("An error occurred: Cannot read cycling-path.csv or file does not exist");
        } catch (SQLException se) {
            se.printStackTrace();
            throw new SQLException("An error occurred: Cannot create or insert into CyclingNetwork table");
        }
    }
    

    private void insertIntoTransitPerformance() throws SQLException, IOException {
        try {
            // Create the TransitPerformance table
            this.connection.createStatement().executeUpdate("CREATE TABLE TransitPerformance ("
                    + "performanceID INT IDENTITY(1,1) PRIMARY KEY, "
                    + "routeID INT, "
                    + "stopID INT, "
                    + "routeDestination VARCHAR(MAX), "
                    + "dayType VARCHAR(MAX), "
                    + "scheduledDate DATE, "
                    + "scheduledTime TIME, "
                    + "deviation INT, "
                    + "FOREIGN KEY(routeID) REFERENCES Routes(routeID), "
                    + "FOREIGN KEY(stopID) REFERENCES Stops(stopID))");
    
            // Reading data from the CSV file
            BufferedReader br = new BufferedReader(new FileReader("final-data-files/Transit-Performance.csv"));
            PreparedStatement pstmt;
            String inputLine;
            String sql;
            String[] inputArr;
    
            br.readLine(); // Skip the headers
    
            // Define date and time format for parsing
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy, h:mm:ss a");
    
            while ((inputLine = br.readLine()) != null) {
                inputArr = inputLine.split(","); // Assuming CSV is comma-separated
    
                // Trim inputs
                String datePart = inputArr[4].trim();
                String timePart = inputArr[5].trim();
    
                // Parse the date and time
                Date parsedDateTime = dateTimeFormat.parse(datePart + ", " + timePart);
                java.sql.Date sqlDate = new java.sql.Date(parsedDateTime.getTime());
                java.sql.Time sqlTime = new java.sql.Time(parsedDateTime.getTime());
    
                // Parse numeric values
                int deviation = Integer.parseInt(inputArr[6].trim());
    
                // Log parsed values for debugging
                System.out.println("Parsed values: routeID=" + inputArr[0] + ", stopID=" + inputArr[1] +
                    ", routeDestination=" + inputArr[2] + ", dayType=" + inputArr[3] +
                    ", scheduledDate=" + sqlDate + ", scheduledTime=" + sqlTime +
                    ", deviation=" + deviation);
    
                // Prepare SQL statement to insert data
                sql = "INSERT INTO TransitPerformance (routeID, stopID, routeDestination, dayType, scheduledDate, scheduledTime, deviation) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                pstmt = connection.prepareStatement(sql);
    
                pstmt.setInt(1, Integer.parseInt(inputArr[0].trim())); // routeID
                pstmt.setInt(2, Integer.parseInt(inputArr[1].trim())); // stopID
                pstmt.setString(3, inputArr[2].trim()); // routeDestination
                pstmt.setString(4, inputArr[3].trim()); // dayType
                pstmt.setDate(5, sqlDate); // scheduledDate
                pstmt.setTime(6, sqlTime); // scheduledTime
                pstmt.setInt(7, deviation); // deviation
    
                pstmt.executeUpdate(); // Execute the insertion
            }
    
            br.close();
            System.out.println("TransitPerformance table created and populated successfully");
        } catch (IOException io) {
            throw new IOException("An error occurred: Cannot read Transit-Performance.csv or file does not exist");
        } catch (ParseException pe) {
            throw new IOException("An error occurred: Invalid date/time format in CSV file");
        } catch (SQLException se) {
            System.out.println("Error creating or populating TransitPerformance table: " + se.getMessage());
            throw se;
        }
    }
    

    

    private void insertIntoPassUps() throws SQLException, IOException {
        try {
            // Create the PassUps table
            this.connection.createStatement().executeUpdate("CREATE TABLE PassUps ("
                    + "passUpID INT IDENTITY(1,1) PRIMARY KEY, " // Use IDENTITY for auto-increment
                    + "routeID INT, "
                    + "streetID INT, "
                    + "passUpType VARCHAR(MAX), " // Use VARCHAR(MAX) instead of TEXT
                    + "dayType VARCHAR(MAX), "
                    + "FOREIGN KEY(routeID) REFERENCES Routes(routeID), "
                    + "FOREIGN KEY(streetID) REFERENCES Streets(streetID))");
    
            // Reading data from the CSV file
            BufferedReader br = new BufferedReader(new FileReader("final-data-files/Pass-Ups.csv"));
            PreparedStatement pstmt;
            String inputLine;
            String sql;
            String[] inputArr;
    
            br.readLine(); // Skip the headers
    
            while ((inputLine = br.readLine()) != null) {
                inputArr = inputLine.split(","); // Assuming CSV is comma-separated
    
                // Prepare SQL statement to insert data
                sql = "INSERT INTO PassUps (routeID, streetID, passUpType, dayType) "
                        + "VALUES (?, ?, ?, ?)";
    
                pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(inputArr[0])); // routeID
                pstmt.setInt(2, Integer.parseInt(inputArr[1])); // streetID
                pstmt.setString(3, inputArr[2]); // passUpType
                pstmt.setString(4, inputArr[3]); // dayType
                pstmt.executeUpdate(); // Execute the insertion
            }
    
            br.close();
            System.out.println("PassUps table created and populated successfully");
        } catch (IOException io) {
            throw new IOException("An error occurred: Cannot read Pass-Ups.csv or file does not exist");
        } catch (SQLException se) {
            se.printStackTrace();
            throw new SQLException("An error occurred: Cannot create or insert into PassUps table");
        }
    }
    


    private void insertIntoPassengerActivity() throws SQLException, IOException {
        try {
            // Create the PassengerActivity table
            this.connection.createStatement().executeUpdate("CREATE TABLE PassengerActivity ("
                    + "activityID INT IDENTITY(1,1) PRIMARY KEY, " // Use IDENTITY for auto-increment
                    + "routeID INT, "
                    + "stopID INT, "
                    + "schedulePeriodName VARCHAR(MAX), " // Use VARCHAR(MAX) instead of TEXT
                    + "schedulePeriodStartDate DATE, " // Use DATE for start date
                    + "schedulePeriodEndDate DATE, " // Use DATE for end date
                    + "dayType VARCHAR(MAX), "
                    + "timePeriod VARCHAR(MAX), "
                    + "averageBoardings FLOAT, " // Use FLOAT for numeric values
                    + "averageAlightings FLOAT, "
                    + "FOREIGN KEY(routeID) REFERENCES Routes(routeID), "
                    + "FOREIGN KEY(stopID) REFERENCES Stops(stopID))");
    
            // Reading data from the CSV file
            BufferedReader br = new BufferedReader(new FileReader("final-data-files/Passenger-Activity.csv"));
            PreparedStatement pstmt;
            String inputLine;
            String sql;
            String[] inputArr;
    
            br.readLine(); // Skip headers
    
            // Define the date format for parsing
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    
            while ((inputLine = br.readLine()) != null) {
                inputArr = inputLine.split(","); // Assuming CSV is comma-separated
    
                // Parse the start and end dates
                Date startDate = dateFormat.parse(inputArr[3]);
                Date endDate = dateFormat.parse(inputArr[4]);
    
                // Convert dates to java.sql.Date
                java.sql.Date sqlStartDate = new java.sql.Date(startDate.getTime());
                java.sql.Date sqlEndDate = new java.sql.Date(endDate.getTime());
    
                // Prepare SQL statement to insert data
                sql = "INSERT INTO PassengerActivity (routeID, stopID, schedulePeriodName, schedulePeriodStartDate, "
                        + "schedulePeriodEndDate, dayType, timePeriod, averageBoardings, averageAlightings) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
                pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(inputArr[0])); // routeID
                pstmt.setInt(2, Integer.parseInt(inputArr[1])); // stopID
                pstmt.setString(3, inputArr[2]); // schedulePeriodName
                pstmt.setDate(4, sqlStartDate); // schedulePeriodStartDate
                pstmt.setDate(5, sqlEndDate); // schedulePeriodEndDate
                pstmt.setString(6, inputArr[5]); // dayType
                pstmt.setString(7, inputArr[6]); // timePeriod
                pstmt.setDouble(8, Double.parseDouble(inputArr[7])); // averageBoardings
                pstmt.setDouble(9, Double.parseDouble(inputArr[8])); // averageAlightings
                pstmt.executeUpdate(); // Execute insertion
            }
    
            br.close();
            System.out.println("PassengerActivity table created and populated successfully");
        } catch (IOException io) {
            throw new IOException("An error occurred: Cannot read Passenger-Activity.csv or file does not exist");
        } catch (ParseException pe) {
            throw new IOException("An error occurred: Invalid date format in Passenger-Activity.csv");
        } catch (SQLException se) {
            se.printStackTrace();
            throw new SQLException("An error occurred: Cannot create or insert into PassengerActivity table");
        }
    }


    private void insertIntoRoutes() throws SQLException, IOException {
        try {
            // Create the Routes table
            this.connection.createStatement().executeUpdate("CREATE TABLE Routes ("
                    + "routeID INT IDENTITY(1,1) PRIMARY KEY, " // Use IDENTITY for auto-increment
                    + "routeNumber INT, "
                    + "routeName VARCHAR(MAX))"); // Use VARCHAR(MAX) instead of TEXT

            // Reading data from the CSV file
            BufferedReader br = new BufferedReader(new FileReader("final-data-files/Routes.csv"));
            PreparedStatement pstmt;
            String inputLine;
            String sql;
            String[] inputArr;

            br.readLine(); // Skip headers

            while ((inputLine = br.readLine()) != null) {
                inputArr = inputLine.split(","); // Assuming CSV is comma-separated

                // Prepare SQL statement to insert data
                sql = "INSERT INTO Routes (routeNumber, routeName) VALUES (?, ?)";

                pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(inputArr[0])); // routeNumber
                pstmt.setString(2, inputArr[1]); // routeName
                pstmt.executeUpdate(); // Execute insertion
            }

            br.close();
            System.out.println("Routes table created and populated successfully");
        } catch (IOException io) {
            throw new IOException("An error occurred: Cannot read Routes.csv or file does not exist");
        } catch (SQLException se) {
            se.printStackTrace();
            throw new SQLException("An error occurred: Cannot create or insert into Routes table");
        }
    }



    private void insertIntoStop() throws SQLException, IOException {
        try {
            // Create the Stops table
            this.connection.createStatement().executeUpdate("CREATE TABLE Stops ("
                    + "stopID INT IDENTITY(1,1) PRIMARY KEY, " // Use IDENTITY for auto-increment
                    + "streetID INT, "
                    + "stopNumber INT, "
                    + "FOREIGN KEY(streetID) REFERENCES Streets(streetID))");
    
            BufferedReader br = new BufferedReader(new FileReader("final-data-files/Stops.csv"));
            PreparedStatement pstmt;
            String inputLine;
            String sql;
            String[] inputArr;
    
            br.readLine(); // Skip headers
    
            while ((inputLine = br.readLine()) != null) {
                inputArr = inputLine.split(",");
    
                sql = "INSERT INTO Stops (streetID, stopNumber) VALUES (?, ?)";
    
                pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(inputArr[0])); // streetID
                pstmt.setInt(2, Integer.parseInt(inputArr[1])); // stopNumber
                pstmt.executeUpdate(); // Execute insertion
            }
    
            br.close();
            System.out.println("Stops table created and populated successfully");
        } catch (IOException io) {
            throw new IOException("An error occurred: Cannot read Stops.csv or file does not exist");
        } catch (SQLException se) {
            se.printStackTrace();
            throw new SQLException("An error occurred: Cannot create or insert into Stops table");
        }
    }
    


    private void insertIntoStreet() throws SQLException, IOException {
        try {
            // Create the Streets table
            this.connection.createStatement().executeUpdate("CREATE TABLE Streets ("
                    + "streetName VARCHAR(MAX), " // Use VARCHAR(MAX) instead of TEXT
                    + "streetID INT IDENTITY(1,1) PRIMARY KEY)"); // Use IDENTITY for auto-increment
    
            BufferedReader br = new BufferedReader(new FileReader("final-data-files/Street.csv"));
            PreparedStatement pstmt;
            String inputLine;
            String sql;
            String[] inputArr;
    
            br.readLine(); // Skip headers
    
            while ((inputLine = br.readLine()) != null) {
                inputArr = inputLine.split(",");
    
                sql = "INSERT INTO Streets (streetName) VALUES (?)";
    
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, inputArr[0]); // streetName
                pstmt.executeUpdate();
            }
    
            br.close();
            System.out.println("Streets table populated successfully");
        } catch (IOException io) {
            throw new IOException("An error occurred: Cannot read Street.csv or file does not exist");
        } catch (SQLException se) {
            se.printStackTrace();
            throw new SQLException("An error occurred: Cannot insert into Streets table");
        }
    }
    


    private void insertIntoTrafficCount() throws SQLException, IOException {
        try {
            // Create the TrafficCounts table
            this.connection.createStatement().executeUpdate("CREATE TABLE TrafficCounts ("
                    + "countID INT IDENTITY(1,1) PRIMARY KEY, " // Use IDENTITY for auto-increment
                    + "streetID INT, "
                    + "countDate DATE, " // Use DATE for countDate
                    + "dayType VARCHAR(MAX), " // Use VARCHAR(MAX) instead of TEXT
                    + "locationDescription VARCHAR(MAX), "
                    + "countDirection VARCHAR(MAX), "
                    + "count15Minutes INT, "
                    + "Configuration VARCHAR(MAX), "
                    + "streetFrom VARCHAR(MAX), "
                    + "streetTo VARCHAR(MAX), "
                    + "FOREIGN KEY(streetID) REFERENCES Streets(streetID))");
    
            BufferedReader br = new BufferedReader(new FileReader("final-data-files/Traffic_Counts.csv"));
            PreparedStatement pstmt;
            String inputLine;
            String sql;
            String[] inputArr;
    
            br.readLine(); // Skip headers
    
            while ((inputLine = br.readLine()) != null) {
                inputArr = inputLine.split(",");
    
                sql = "INSERT INTO TrafficCounts (streetID, countDate, dayType, locationDescription, countDirection, "
                        + "count15Minutes, Configuration, streetFrom, streetTo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
                pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(inputArr[0])); // streetID
                pstmt.setDate(2, java.sql.Date.valueOf(inputArr[1])); // countDate
                pstmt.setString(3, inputArr[2]); // dayType
                pstmt.setString(4, inputArr[3]); // locationDescription
                pstmt.setString(5, inputArr[4]); // countDirection
                pstmt.setInt(6, Integer.parseInt(inputArr[5])); // count15Minutes
                pstmt.setString(7, inputArr[6]); // Configuration
                pstmt.setString(8, inputArr[7]); // streetFrom
                pstmt.setString(9, inputArr[8]); // streetTo
                pstmt.executeUpdate();
            }
    
            br.close();
            System.out.println("TrafficCounts table populated successfully");
        } catch (IOException io) {
            throw new IOException("An error occurred: Cannot read Traffic_Counts.csv or file does not exist");
        } catch (SQLException se) {
            se.printStackTrace();
            throw new SQLException("An error occurred: Cannot insert into TrafficCounts table");
        }
    }
    

    private void insertIntoSpeedLimits() throws SQLException, IOException {
        try {
            // Create the SpeedLimits table
            this.connection.createStatement().executeUpdate("CREATE TABLE SpeedLimits ("
                    + "streetID INT, "
                    + "speedLimit INT, "
                    + "speedLimitDesc VARCHAR(MAX), " // Use VARCHAR(MAX) instead of TEXT
                    + "beginMeasure FLOAT, " // Use FLOAT for numeric values
                    + "endMeasure FLOAT, " // Use FLOAT for numeric values
                    + "jurisdiction VARCHAR(MAX), " // Use VARCHAR(MAX) instead of TEXT
                    + "FOREIGN KEY(streetID) REFERENCES Streets(streetID))");
    
            BufferedReader br = new BufferedReader(new FileReader("final-data-files/Speed-Limits.csv"));
            PreparedStatement pstmt;
            String inputLine;
            String sql;
            String[] inputArr;
    
            br.readLine(); // Skip headers
    
            while ((inputLine = br.readLine()) != null) {
                inputArr = inputLine.split(",");
    
                sql = "INSERT INTO SpeedLimits (streetID, speedLimit, speedLimitDesc, beginMeasure, endMeasure, jurisdiction) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";
    
                pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(inputArr[0])); // streetID
                pstmt.setInt(2, Integer.parseInt(inputArr[1])); // speedLimit
                pstmt.setString(3, inputArr[2]); // speedLimitDesc
                pstmt.setDouble(4, Double.parseDouble(inputArr[3])); // beginMeasure
                pstmt.setDouble(5, Double.parseDouble(inputArr[4])); // endMeasure
                pstmt.setString(6, inputArr[5]); // jurisdiction
                pstmt.executeUpdate();
            }
    
            br.close();
            System.out.println("SpeedLimits table populated successfully");
        } catch (IOException io) {
            throw new IOException("An error occurred: Cannot read Speed-Limits.csv or file does not exist");
        } catch (SQLException se) {
            se.printStackTrace();
            throw new SQLException("An error occurred: Cannot insert into SpeedLimits table");
        }
    }
    


    public String dropAllTables() {
        String response = null;
        try {
            // Dropping each table based on new schema
            PreparedStatement pstmt;

            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS TransitPerformance;");
            pstmt.executeUpdate();

            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS PassengerActivity;");
            pstmt.executeUpdate();

            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS PassUps;");
            pstmt.executeUpdate();

            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS TrafficCounts;");
            pstmt.executeUpdate();

            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS Stops;");
            pstmt.executeUpdate();

            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS SpeedLimits;");
            pstmt.executeUpdate();

            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS Routes;");
            pstmt.executeUpdate();

            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS CyclingNetwork;");
            pstmt.executeUpdate();

            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS Streets;");
            pstmt.executeUpdate();


            System.out.println("All tables dropped successfully.");
        } catch (SQLException se) {
            System.out.println("Error while deleting the tables");
            se.printStackTrace();
            response = "An Error occurred: Something went wrong while deleting tables";
        }

        return response;

    }



}

    

