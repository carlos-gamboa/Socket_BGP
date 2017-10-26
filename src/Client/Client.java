package Client;

import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class Client extends Thread {

    private Map<String, Integer> neighbours;
    private ArrayList<String> networks;
    private ListMultimap<String, ArrayList<Integer>> routesMultimap;
    private Integer as_ID;

    public Client (Integer id, String ip, Integer client_Port, Map<String, Integer> neighbours, ArrayList<String>  networks, ListMultimap<String, ArrayList<Integer>> routesMultimap) {
        this.neighbours = neighbours;
        this.networks = networks;
        this.routesMultimap = routesMultimap;
        this.as_ID = id;
    }

    public synchronized String routesToString(Integer neighbourAS) {
        String message = String.valueOf(as_ID) + "*";
        for (String key : routesMultimap.keySet()) {
            Collection<ArrayList<Integer>> values = routesMultimap.get(key);
            for (Iterator<ArrayList<Integer>> iterator = values.iterator(); iterator.hasNext();) {
                ArrayList<Integer> route = iterator.next();
                if (route.get(0) == neighbourAS){
                    message = message + key + ":" + arrayToString(route) + ",";
                }
            }
        }
        return message;
    }

    public synchronized String routesToString() {
        String message = String.valueOf(as_ID) + "*";
        for (String key : routesMultimap.keySet()) {
            Collection<ArrayList<Integer>> values = routesMultimap.get(key);
            for (Iterator<ArrayList<Integer>> iterator = values.iterator(); iterator.hasNext();) {
                ArrayList<Integer> route = iterator.next();
                message = message + key + ":" + arrayToString(route) + ",";
            }
        }
        return message;
    }

    private String arrayToString(ArrayList<Integer> routes) {
        String result = "";
        for (int i = 0; i < routes.size(); ++i) {
            result = result + "AS" + String.valueOf(routes.get(i));
            if (i != routes.size() - 1) {
                result = result + "-";
            }
        }
        return result;
    }

    @Override
    public void run() {

    }
}
