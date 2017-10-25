package Server;

import com.google.common.collect.ListMultimap;
import lombok.extern.java.Log;
import lombok.val;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

@Log
public class Server extends Thread {

    private Map<String, Integer> neighbours;
    private ArrayList<String> networks;
    private ListMultimap<String, String> routesMultimap;
    private int port;

    public Server (Integer port, Map<String, Integer> neighbours, ArrayList<String>  networks, ListMultimap<String, String> routesMultimap) {
        this.neighbours = neighbours;
        this.networks = networks;
        this.routesMultimap = routesMultimap;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket socket = new ServerSocket(port);
            while (true) {
                try {
                    Socket mini_Server = socket.accept();
                    log.info("Device " + mini_Server.getInetAddress() + " connected");

                    ObjectInputStream objectInputStream = new ObjectInputStream(mini_Server.getInputStream());
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(mini_Server.getOutputStream());


                } catch (IOException ex) {
                    log.warning("Cannot connect device");
                }
            }
        }
        catch (IOException e) {
            log.warning("Cannot create server");
        }
    }

    public void listen () {

    }

}
