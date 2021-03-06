import com.google.common.collect.ListMultimap;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class Client extends Thread {

    private String hostName; //Other AS's ip
    private int portNumber; //Port to communicate to the other AS
    private boolean clientIsOn; //To indicate if the client is active
    private BufferedReader in; //To write on a socket
    private PrintWriter out; //To read from a socket
    private Socket echoSocket; //Client's socket
    private volatile Routes_Manager routes_Manager; ////Instance used to update all the routes
    private Integer as_ID; //AS's id
    private Integer serverAS; //Server's id
    private File_Manager log_file;


    /**
     * Creates a client thread, to allow the AS to communicate with its neighbours
     * @param id AS's id
     * @param ip Other AS's ip
     * @param client_Port Port to communicate to the other AS
     * @param neighbours Map that contains the associated AS's and the port for each one
     * @param networks Array with the AS's known networks
     * @param routesMultimap Map that contains the network and an array with the other AS's id that are part of the route
     */
    public Client (Integer id, String ip, Integer client_Port, Map<String, Integer> neighbours, ArrayList<String>  networks, ListMultimap<String, ArrayList<Integer>> routesMultimap, File_Manager log_file, Map<String, Integer> shortestRoutes) {
        this.hostName = ip;
        this.portNumber = client_Port;
        this.clientIsOn = true;
        routes_Manager = new Routes_Manager(id, neighbours, networks, routesMultimap, shortestRoutes);
        this.as_ID = id;
        this.serverAS = -1;
        this.log_file = log_file;
    }

    /**
     * Manages the messages to update the routes
     *  1- Send a message with the routes
     *  2- Wait 30s to receive a message from the server in response
     * @throws IOException
     */
    public void manageUpdateMessages() throws IOException {
        //Creates a buffer to read a message from the server socket
        if (this.in == null) {
            this.in = new BufferedReader(new InputStreamReader(this.echoSocket.getInputStream()));
        }
        //Creates a buffer to write a message to the server socket
        if (this.out == null) {
            this.out = new PrintWriter(this.echoSocket.getOutputStream(), true);
        }

        //Sends message
        if (serverAS == -1) {
            this.out.println(this.routes_Manager.routesToString()); //Writes the routes message to the buffer
        }
        else {
            this.out.println(this.routes_Manager.routesToString(serverAS)); //Writes the routes message to the buffer
        }

        String message = "";

        //Obtains time
        long initialTime = System.currentTimeMillis();
        long currentTime = initialTime;

        //Receive message in response
        while (currentTime - initialTime <= 30000 && message.equals("")) { //Control time between messages
            message = this.in.readLine(); //Reads the message from the buffer
            currentTime = System.currentTimeMillis();
        }

        //If there is no message in that time the connection is lost, else the AS's routes are updated
        if (message == null) {
            this.timeout();
        } else {
            if (serverAS == -1){
                serverAS = routes_Manager.getASIDFromMessage(message);
                System.err.println("Connected to Server AS"+serverAS);
            }
            routes_Manager.updateRoutes(message);
        }
    }

    /**
     * Closes the buffers used in the communication and close the client socket
     */
    void kill() {

        if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException e) {
                System.err.println("Client connection couldn't be closed (input error)");
                log_file.writeToFile("Client connection couldn't be closed (input error).");
            }
        }

        if (this.out != null) {
            this.out.close();
        }

        if (this.echoSocket != null) {
            try {
                this.echoSocket.close();
            } catch (IOException e) {
                System.err.println("Client connection couldn't be closed (client socket error)");
                log_file.writeToFile("Client connection couldn't be closed (client socket error).");
            }
        }

        this.clientIsOn = false;

    }

    /**
     * Verifies the AS of the lost connection and removes all the routes which include it
     */
    private void timeout () {
        if (serverAS == -1) {
            System.err.println("No connection with the server.");
            log_file.writeToFile("No connection with the server.");
        }
        else {
            System.err.println("AS" + serverAS + " has timed out.");
            routes_Manager.removeRoutesFromAS(serverAS);
            log_file.writeToFile("AS" + serverAS + " has timed out.");
        }
    }

    /**
     * Client thread's function, sends a message each 30s to its neighbours and receive one in response
     */
    @Override
    public void run() {
        while (true) {
            try {
                this.in = null;
                this.out = null;
                this.serverAS = -1;
                this.echoSocket = new Socket(hostName, portNumber);
                while (clientIsOn) {
                    manageUpdateMessages();
                    Thread.sleep(20000); //wait 20s
                }
            } catch (UnknownHostException e) {
                timeout();
            } catch (IOException e) {
                timeout();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(20000); //wait 20s
            }  catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}