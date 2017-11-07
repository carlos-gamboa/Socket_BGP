import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import lombok.extern.java.Log;
import lombok.val;

import Server.Server;
import Client.Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Log
public class AutonomousSystem {

    private volatile Map<String, Integer> neighbours; //Map that contains the associated AS's and the port for each one
    private volatile ArrayList<String> networks; //Array with the AS's known networks
    private Boolean on; //Indicate if the AS is running
    private volatile ListMultimap<String, ArrayList<Integer>> routesMultimap; //Map that contains the network and an array with the other AS's id that are part of the route
    private int id; //Identifier of the AS

    /**
     * Creates an AutonomousSystem
     */
    public AutonomousSystem () {
        on = false;
        networks = new ArrayList<>();
        neighbours = new HashMap<>();
        routesMultimap = ArrayListMultimap.create();
        System.out.println("The server is running.");
    }

    /**
     * Verifies if the AS is running
     * @return on
     */
    private Boolean isOn () {
        return on;
    }

    /**
     * Reads a file for create an AS
     * @param file with the AS information
     * @return Tru if the file was opened. False if the file wasn't opened.
     */
    private Boolean loadFile (String file) {
        int port;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            int type = 0;
            //Interprets file's information
            while (line != null) {
                if (line.equals("#Redes conocidas")){
                    type = 1;
                }
                else if (line.equals("#Vecinos BGP")) {
                    type = 2;
                }
                else if (line.equals("#Escuchar vecinos")) {
                    type = 3;
                }
                else if (line.equals("#ID")){
                    type = 4;
                }
                else {
                    if(type == 1){
                        networks.add(line);
                    } else if (type == 2) {
                        //Esto sirve solo para un vecino
                        StringTokenizer tokens = new StringTokenizer(line, ":");
                        String ip = tokens.nextToken();
                        Integer client_Port = Integer.parseInt(tokens.nextToken());
                        neighbours.put(ip, client_Port);
                        new Client(id, ip, client_Port, neighbours, networks, routesMultimap).start();
                    }
                    else if (type == 3) {
                        port = Integer.parseInt(line);
                        new Server(port, neighbours, networks, routesMultimap).start();
                    }
                    else if (type == 4) {
                        id = Integer.parseInt(line);
                    }
                }
                line = br.readLine();
            }
            br.close();
        }
        catch (IOException e) {
            System.out.print("File not found. Enter AS information filename: ");
            return false;
        }
        return true;
    }

    /**
     * Reads th file's name
     * @param sc Scanner for read-write operations
     */
    private void startAS (Scanner sc) {
        Boolean done;
        System.out.print("Enter AS information filename: ");
        do {
            String file = sc.nextLine();
            done = loadFile(file);
        } while (!done);
    }

    /**
     * Allows th user to use specified commands on the terminal
     */
    public void startTerminal () {
        val sc = new Scanner(System.in);
        startAS(sc);
        System.out.print("> ");
        String line;
        while (true) {
            line = sc.nextLine();
            StringTokenizer tokens = new StringTokenizer(line, ">");
            String command = tokens.nextToken();
            //Creo que no estamos tomando bien la subred
            String network = "";
            if (tokens.hasMoreTokens()){
                network = tokens.nextToken();
                network = network.replace(">", "");
            }
            switch (command) {
                case "stop":
                    stop();
                    break;
                case "add":
                    add(network);
                    break;
                case "show routes":
                    show_Route();
                    break;
                case "start":
                    start();
                    break;
                default:
                    log.info("Unknown command");
                    break;
            }
        }
    }

    /**
     * Stops the AS if it's running
     */
    private void stop() {
        if (isOn()) {
            on = false;
            log.info("The server has stopped");
        }
    }

    /**
     * Adds specified networks to the AS's known networks if the AS is running
     * @param network
     */
    private void add (String network) {
        if (isOn()) {
            networks.add(network);
        }
    }

    /**
     * Shows all the routes from the AS to the other networks if the AS is running
     */
    private void show_Route() {
        if (isOn()) {
            for (String key : routesMultimap.keySet()) { //Iterate over the networks
                for (ArrayList<Integer> value : routesMultimap.get(key)) { //Iterate over the routes array of each network
                    System.out.println("Red " + key + ": " + routesArrayToString(value));
                }
            }
        }
    }

    /**
     * Converts the routes array to an specific type of string
     * @param routes an array of the AS's id that are part of a route
     * @return result a String with the desired content
     */
    private String routesArrayToString(ArrayList<Integer> routes) {
        String result = "";
        for (int i = 0; i < routes.size(); ++i) {
            result = result + "AS" + String.valueOf(routes.get(i));
            if (i != routes.size() - 1) {
                result = result + "-";
            }
        }
        return result;
    }

    /**
     * Starts the AS's operation
     */
    private void start (){
        if (!isOn()) {
            on = true;
            log.info("The server has started");
        }
    }
}