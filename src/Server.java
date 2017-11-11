
import com.google.common.collect.ListMultimap;
import lombok.extern.java.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

@Log
public class Server extends Thread {

    private int port; //Port to listen the neighbours
    private volatile Routes_Manager routes_Manager; //Instance used to update all the routes
    private ServerSocket server; //Server socket
    private ArrayList<Connections> connections; //Array with all the sockets created
    private Boolean isOn; //To indicate if the client is active
    private PrintWriter log_file;

    /**
     * Creates a client thread, to allow the AS to communicate with its neighbours
     *
     * @param id             AS id.
     * @param port           Port to listen the neighbours
     * @param neighbours     Map that contains the associated AS's and the port for each one
     * @param networks       Array with the AS's known networks
     * @param routesMultimap Map that contains the network and an array with the other AS's id that are part of the route
     */
    public Server(Integer id, Integer port, Map<String, Integer> neighbours, ArrayList<String> networks, ListMultimap<String, ArrayList<Integer>> routesMultimap, PrintWriter log_file) {
        routes_Manager = new Routes_Manager(id, neighbours, networks, routesMultimap);
        this.port = port;

        this.connections = new ArrayList<>();

        this.log_file = log_file;

        try {
            this.server = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Couldn't create server socket.");
            log_file.println("Couldn't create server socket.");
        }
    }

    /**
     * Manages the new connections, adds them to the array and start them
     */
    private void manageConnections() {

        while (this.isOn) {
            try {
                Socket mini_Server = this.server.accept();

                Connections connection = new Connections(mini_Server, routes_Manager, log_file);
                this.connections.add(connection);
                connection.start();
            } catch (IOException e) {
                System.err.println("Finished connection.");
                log_file.println("Finished connection.");
            }

        }

    }

    /**
     * Closes the main server socket and its connections
     */
    void kill() {
        for (Connections connection : this.connections) {
            connection.kill();
            System.err.println("Server connection: OFF");
            log_file.println("Server connection: OFF.");
        }

        this.isOn = false;
        boolean closed = false;
        while (!closed) {
            try {
                this.server.close();
                closed = true;
            } catch (IOException e) {
                System.err.println("Couldn't close the server socket, retrying...");
                log_file.println("Couldn't close the server socket.");
            }
        }

    }

    /**
     * Main server thread, if it's on, add connections
     */
    @Override
    public void run(){

        this.isOn = true;
        while (this.isOn) {
            this.manageConnections();
        }
    }
}