package Client;

import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.Map;

public class Client extends Thread {

    private Map<String, Integer> neighbours;
    private ArrayList<String> networks;
    private ListMultimap<String, ArrayList<Integer>> routesMultimap;

    public Client (String ip, Integer client_Port, Map<String, Integer> neighbours, ArrayList<String>  networks, ListMultimap<String, ArrayList<Integer>> routesMultimap) {
        this.neighbours = neighbours;
        this.networks = networks;
        this.routesMultimap = routesMultimap;
    }

    @Override
    public void run() {

    }
}
