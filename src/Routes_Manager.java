import com.google.common.collect.ListMultimap;

import java.util.*;

public class Routes_Manager {
    private Map<String, Integer> neighbours; //Map that contains the associated AS's and the port for each one
    private Map<String, Integer> shortestRoutes;
    private ArrayList<String> networks; //Array with the AS's known networks
    private ListMultimap<String, ArrayList<Integer>> routesMultimap; //Map that contains the network and an array with the other AS's id that are part of the route
    private Integer as_ID; //Identifier of the AS
    private String hostName; //Other AS's ip
    private ArrayList<String> keysToDelete;
    private ArrayList<ArrayList<Integer>> valuesToDelete;

    /**
     * Creates a Route Manager to manage the updates.
     * @param id AS's id
     * @param neighbours Map that contains the associated AS's and the port for each one
     * @param networks Array with the AS's known networks
     * @param routesMultimap Map that contains the network and an array with the other AS's id that are part of the route
     */
    public Routes_Manager (Integer id, Map<String, Integer> neighbours, ArrayList<String>  networks, ListMultimap<String, ArrayList<Integer>> routesMultimap, Map<String, Integer> shortestRoutes) {
        this.neighbours = neighbours;
        this.networks = networks;
        this.routesMultimap = routesMultimap;
        this.as_ID = id;
        this.shortestRoutes = shortestRoutes;
        this.keysToDelete = new ArrayList<>();
        this.valuesToDelete = new ArrayList<>();
    }

    /**
     * Updates the routes multimap with the coming message
     * @param message A string with the updated route
     */
    public Integer getASIDFromMessage (String message){
        StringTokenizer tokens = new StringTokenizer(message, "*");
        String asID = tokens.nextToken();
        asID = asID.replace("AS", "");
        return Integer.valueOf(asID);
    }

    /**
     * Converts the routes multimap into a string with the specified format
     * @param neighbourAS The id of the neighbour AS
     * @return message A string with all the route information
     */
    public synchronized String routesToString(Integer neighbourAS) {
        String message = "AS" + String.valueOf(as_ID) + "*"; //Concatenate the AS's id
        for (String key : routesMultimap.keySet()) { //Iterates over the networks
            Collection<ArrayList<Integer>> values = routesMultimap.get(key); //Set of all the routes for each network
            for (Iterator<ArrayList<Integer>> iterator = values.iterator(); iterator.hasNext();) { //Iterates over the set of routes
                ArrayList<Integer> route = iterator.next();
                if (route.isEmpty()){
                    message = message + key + ":AS" + as_ID + ",";
                }
                else if (!route.contains(neighbourAS)){
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
        String message = "AS" + String.valueOf(as_ID) + "*"; //Concatenate the AS's id
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
    private synchronized String arrayToString(ArrayList<Integer> routes) {
        String result = "AS" + as_ID;
        for (int i = 0; i < routes.size(); ++i) { //Iterates over the routes array
            result = result + "-AS" + String.valueOf(routes.get(i)); //Add each AS's id
        }
        return result;
    }

    /**
     * Removes routes associate with an specified AS
     * @param as The AS's id
     */
    public synchronized void removeRoutesFromAS(Integer as){
        keysToDelete.clear();
        valuesToDelete.clear();
        String realKey = "";
        Iterator<String> key = routesMultimap.keySet().iterator();
        while (key.hasNext()) { //Iterates over the networks
            realKey = key.next();
            Collection<ArrayList<Integer>> values = routesMultimap.get(realKey); //Set of all the routes for each network
            for (Iterator<ArrayList<Integer>> iterator = values.iterator(); iterator.hasNext();) { //Iterates over the set of routesC:\Users\Dell\Documents\Universidad\Redes de Computadores\Socket_BGP\src
                ArrayList<Integer> route = iterator.next();
                if (!route.isEmpty()){
                    if (route.get(0) == as){ //If the route contains the specified AS, remove it
                        keysToDelete.add(realKey);
                        valuesToDelete.add(route);
                    }
                }
            }
        }
        for (int i = 0; i < keysToDelete.size(); ++i){
            routesMultimap.remove(keysToDelete.get(i), valuesToDelete.get(i));
        }
    }

    /**
     * Converts a string message of a route to the routes array
     * @param routes String with the specified format of a route
     * @return routes_array An array with the route
     */
    public synchronized ArrayList<Integer> routeStringToArray (String routes){
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
        as_ID = as_ID.replace("AS", "");
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
                ArrayList<Integer> routesArray = routeStringToArray(routes_From_IP);
                routesMultimap.put(route_IP, routesArray);
                if (!shortestRoutes.containsKey(route_IP) || shortestRoutes.get(route_IP) > routesArray.size()) {
                    shortestRoutes.put(route_IP, routesArray.size());
                }
            }
        }
    }
}
