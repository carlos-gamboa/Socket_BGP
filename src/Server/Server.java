package Server;

import com.google.common.collect.ListMultimap;
import lombok.extern.java.Log;
import lombok.val;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

@Log
public class Server extends Thread {

    private Map<String, Integer> neighbours; //Map that contains the associated AS's and the port for each one
    private ArrayList<String> networks; //Array with the AS's known networks
    private ListMultimap<String, ArrayList<Integer>> routesMultimap; //Map that contains the network and an array with the other AS's id that are part of the route
    private int port; //Port to listen the neighbours

    /**
     * Creates a client thread, to allow the AS to communicate with its neighbours
     * @param port Port to listen the neighbours
     * @param neighbours Map that contains the associated AS's and the port for each one
     * @param networks Array with the AS's known networks
     * @param routesMultimap Map that contains the network and an array with the other AS's id that are part of the route
     */
    public Server (Integer port, Map<String, Integer> neighbours, ArrayList<String>  networks, ListMultimap<String, ArrayList<Integer>> routesMultimap) {
        this.neighbours = neighbours;
        this.networks = networks;
        this.routesMultimap = routesMultimap;
        this.port = port;
    }

    /**
     * Removes routes associate with an specified AS
     * @param as The AS's id
     */
    public synchronized void removeRoutesFromAS(Integer as){
        for (String key : routesMultimap.keySet()) { //Iterates over the networks
            Collection<ArrayList<Integer>> values = routesMultimap.get(key); //Set of all the routes for each network
            for (Iterator<ArrayList<Integer>> iterator = values.iterator(); iterator.hasNext();) { //Iterates over the set of routes
                ArrayList<Integer> route = iterator.next();
                if (!route.isEmpty()){
                    if (route.get(0) == as){ //If the route contains the specified AS, remove it
                        routesMultimap.remove(key, route);
                    }
                }
            }
        }
    }

    /**
     * Converts a string message of a route to the routes array
     * @param routes String with the specified format of a route
     * @return routes_array An array with the route
     */
    public ArrayList<Integer> routeStringToArray (String routes){
        ArrayList<Integer> routes_Array = new ArrayList<>();
        StringTokenizer routesTokens = new StringTokenizer(routes, "-");
        while(routesTokens.hasMoreTokens()){
            String as = routesTokens.nextToken();
            Integer as_ID = Integer.parseInt(as.substring(as.length() - 1));
            routes_Array.add(as_ID);
        }
        return routes_Array;
    }

    /**
     * Updates the routes multimap with the coming message
     * @param message A string with the updated route
     */
    public synchronized void updateRoutes(String message){
        StringTokenizer tokens = new StringTokenizer(message, "*");
        String as_ID = tokens.nextToken();
        removeRoutesFromAS(Integer.parseInt(as_ID));
        if (tokens.hasMoreTokens()) {
            String other = tokens.nextToken();
            String routes;
            tokens = new StringTokenizer(other, ",");
            while (tokens.hasMoreTokens()) {
                routes = tokens.nextToken();
                StringTokenizer routesToken = new StringTokenizer(routes, ":");
                String route_IP = routesToken.nextToken();
                String routes_From_IP = routesToken.nextToken();
                routesMultimap.put(route_IP, routeStringToArray(routes_From_IP));
            }
        }
    }

    /**
     * Server thread's function, receives messages of its neighbours
     */
    @Override
    public void run() {
        try {
            ServerSocket socket = new ServerSocket(port);
            while (true) {
                try {
                    Socket mini_Server = socket.accept(); //create connection between AS's
                    System.err.println("Device " + mini_Server.getInetAddress() + " connected");

                    String message = new BufferedReader(new InputStreamReader(mini_Server.getInputStream())).readLine(); //receive a message with the updated route
                    updateRoutes(message);

                } catch (IOException ex) {
                    System.err.println("Cannot connect device");
                }
            }
        }
        catch (IOException e) {
            System.err.println("Cannot create server");
        }
    }

}
