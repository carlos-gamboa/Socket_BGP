package Client;

import com.google.common.collect.ListMultimap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

public class Client extends Thread {

    private Map<String, Integer> neighbours;
    private ArrayList<String> networks;
    private ListMultimap<String, String> routesMultimap;
    private String hostName;
    private int portNumber;

    public Client (String ip, Integer client_Port, Map<String, Integer> neighbours, ArrayList<String>  networks, ListMultimap<String, String> routesMultimap) {
        this.neighbours = neighbours;
        this.networks = networks;
        this.routesMultimap = routesMultimap;
        this.hostName = ip;
        this.portNumber = client_Port;
    }

    @Override
    public void run() {

        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out =
                        new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn =
                        new BufferedReader(
                                new InputStreamReader(System.in))
        ) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("echo: " + in.readLine());
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }

    }
}
