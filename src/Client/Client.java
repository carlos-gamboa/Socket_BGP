package Client;

import com.google.common.collect.ListMultimap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class Client extends Thread {

    private Map<String, Integer> neighbours; //Map that contains the associated AS's and the port for each one
    private ArrayList<String> networks; //Array with the AS's known networks
    private ListMultimap<String, ArrayList<Integer>> routesMultimap; //Map that contains the network and an array with the other AS's id that are part of the route
    private Integer as_ID; //Identifier of the AS
    private String hostName; //Other AS's ip
    private int portNumber; //Port to communicate to the other AS
    private boolean clientIsOn; //To indicate if the client is active
    private String userInput; //To send the message

    /**
     * Creates a client thread, to allow the AS to communicate with its neighbours
     * @param id AS's id
     * @param ip Other AS's ip
     * @param client_Port Port to communicate to the other AS
     * @param neighbours Map that contains the associated AS's and the port for each one
     * @param networks Array with the AS's known networks
     * @param routesMultimap Map that contains the network and an array with the other AS's id that are part of the route
     */
    public Client (Integer id, String ip, Integer client_Port, Map<String, Integer> neighbours, ArrayList<String>  networks, ListMultimap<String, ArrayList<Integer>> routesMultimap) {
        this.neighbours = neighbours;
        this.networks = networks;
        this.routesMultimap = routesMultimap;
        this.as_ID = id;
        this.hostName = ip;
        this.portNumber = client_Port;
        this.clientIsOn = true;
    }

    /**
     * Converts the routes multimap into a string with the specified format
     * @param neighbourAS The id of the neighbour AS
     * @return message A string with all the route information
     */
    public synchronized String routesToString(Integer neighbourAS) {
        String message = String.valueOf(as_ID) + "*"; //Concatenate the AS's id
        for (String key : routesMultimap.keySet()) { //Iterates over the networks
            Collection<ArrayList<Integer>> values = routesMultimap.get(key); //Set of all the routes for each network
            for (Iterator<ArrayList<Integer>> iterator = values.iterator(); iterator.hasNext();) { //Iterates over the set of routes
                ArrayList<Integer> route = iterator.next();
                if (route.get(0) == neighbourAS){ //?
                    message = message + key + ":" + arrayToString(route) + ",";
                }
            }
        }
        return message;
    }

    /**
     * Converts the routes multimap into a string with the specified format
     * @return message A string with all the route information
     */
    public synchronized String routesToString() {
        String message = String.valueOf(as_ID) + "*"; //Concatenate the AS's id
        for (String key : routesMultimap.keySet()) { //Iterates over the networks
            Collection<ArrayList<Integer>> values = routesMultimap.get(key); //Set of all the routes for each network
            for (Iterator<ArrayList<Integer>> iterator = values.iterator(); iterator.hasNext();) {  //Iterates over the set of routes
                ArrayList<Integer> route = iterator.next();
                message = message + key + ":" + arrayToString(route) + ",";
            }
        }
        return message;
    }

    /**
     * Converts the routes array into a string with the specified format
     * @param routes The route array with the AS's id
     * @return result A string with the route
     */
    private String arrayToString(ArrayList<Integer> routes) {
        String result = "";
        for (int i = 0; i < routes.size(); ++i) { //Iterates over the routes array
            result = result + "AS" + String.valueOf(routes.get(i)); //Add each AS's id
            if (i != routes.size() - 1) {
                result = result + "-";
            }
        }
        return result;
    }

    /**
     * Client thread's function, sends a message each 30s to its neighbours
     */
    @Override
    public void run() {
        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(),true);
        ) {
            while (clientIsOn) {
                userInput = routesToString(); //creates the message with the updated route
                out.println(userInput); //send the message
                Thread.sleep(30000); //wait 30s
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}