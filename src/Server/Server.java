package Server;

import com.google.common.collect.ListMultimap;
import lombok.extern.java.Log;
import lombok.val;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

@Log
public class Server extends Thread {

    private Map<String, Integer> neighbours;
    private ArrayList<String> networks;
    private ListMultimap<String, ArrayList<Integer>> routesMultimap;
    private int port;

    public Server (Integer port, Map<String, Integer> neighbours, ArrayList<String>  networks, ListMultimap<String, ArrayList<Integer>> routesMultimap) {
        this.neighbours = neighbours;
        this.networks = networks;
        this.routesMultimap = routesMultimap;
        this.port = port;
    }

    public synchronized void removeRoutesFromAS(Integer as){
        for (String key : routesMultimap.keySet()) {
            Collection<ArrayList<Integer>> values = routesMultimap.get(key);
            for (Iterator<ArrayList<Integer>> iterator = values.iterator(); iterator.hasNext();) {
                ArrayList<Integer> route = iterator.next();
                if (route.get(0) == as){
                    routesMultimap.remove(key, route);
                }
            }
        }
    }

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

    @Override
    public void run() {
        try {
            ServerSocket socket = new ServerSocket(port);
            while (true) {
                try {
                    Socket mini_Server = socket.accept();
                    log.info("Device " + mini_Server.getInetAddress() + " connected");

                    String message = mini_Server.getInputStream().toString();
                    updateRoutes(message);

                } catch (IOException ex) {
                    log.warning("Cannot connect device");
                }
            }
        }
        catch (IOException e) {
            log.warning("Cannot create server");
        }
    }

}
