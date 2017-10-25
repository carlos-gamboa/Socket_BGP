import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.sun.org.apache.xpath.internal.operations.Bool;
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

    private static int port;
    private volatile Map<String, Integer> neighbours;
    private volatile ArrayList<String> networks;
    private static Boolean on;
    private volatile ListMultimap<String, String> routesMultimap;

    private static Boolean isOn () {
        return on;
    }

    private static void loadFile (String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            int type = 0;

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
                else {
                    if(type == 1){
                        networks.add(line);
                    } else if (type == 2) {
                        StringTokenizer tokens = new StringTokenizer(line, ":");
                        String ip = tokens.nextToken();
                        Integer client_Port = Integer.parseInt(tokens.nextToken());
                        neighbours.put(ip, client_Port);
                        new Client(ip, client_Port, neighbours, networks, routesMultimap).start();
                    }
                    else if (type == 3) {
                        port = Integer.parseInt(line);
                        new Server(port, neighbours, networks, routesMultimap).start();
                    }
                }
                line = br.readLine();
            }
            br.close();
        }
        catch (IOException e) {
            System.out.print("Could not find the file.\nEnter AS information filename: ");
        }
    }

    private static void startAS (Scanner sc) {
        System.out.print("Enter AS information filename: ");
        String file = sc.nextLine();
        loadFile(file);
    }

    private static void startTerminal () {
        val sc = new Scanner(System.in);
        startAS(sc);
        System.out.print("> ");
        String line = "";
        while (true) {
            line = sc.nextLine();
            StringTokenizer tokens = new StringTokenizer(line, "<");
            String command = tokens.nextToken();
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

    private static void stop() {
        if (isOn()) {
            on = false;
            log.info("The server has stopped");
        }
    }

    private static void add (String network) {
        if (isOn()) {
            networks.add(network);
        }
    }

    private static void show_Route() {
        if (isOn()) {
            for (String key : routesMultimap.keySet()) {
                for (String value : routesMultimap.get(key)) {
                    System.out.println("Red " + key + ": " + value);
                }
            }
        }
    }

    private static void start (){
        if (!isOn()) {
            on = true;
            log.info("The server has started");
        }
    }

    public static void main(String[] args) {
        on = false;
        networks = new ArrayList<>();
        neighbours = new HashMap<>();
        routesMultimap = ArrayListMultimap.create();

        log.info("The Server is running.");
        startTerminal();
    }
}
