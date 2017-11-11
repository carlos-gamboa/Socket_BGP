import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connections extends Thread {

    private Integer as_ID; //AS's id
    private Socket mini_Server; //Server socket created
    private boolean isOn; //To indicate if the client is active
    private Routes_Manager manager; //Instance used to update all the routes
    private BufferedReader in; //To write on a socket
    private PrintWriter out; //To read from a socket
    private PrintWriter log_file;

    /**
     * Creates the connections of a server
     * @param clientSocket socket to connect with the client
     * @param manager to update the routes
     */
    Connections (Socket clientSocket, Routes_Manager manager, PrintWriter log_file) {
        this.mini_Server = clientSocket;
        this.manager = manager;
        this.as_ID = -1;
        this.log_file = log_file;
    }

    /**
     * Manages messages
     * 1- Receives a message with the routes every 30s
     * 2- Sends one in response if it receive it
     * @throws IOException, NullPointerException
     */
    private void manageMessages() throws IOException, NullPointerException {
        //Creates a buffer to read a message from the client socket
        if (this.in == null) {
            this.in = new BufferedReader(new InputStreamReader(mini_Server.getInputStream()));
        }
        //Creates a buffer to write a message to a client socket
        if (this.out == null) {
            this.out = new PrintWriter(mini_Server.getOutputStream(), true);
        }

        String message = "";

        //Obtains time
        long initialTime = System.currentTimeMillis();
        long currentTime = initialTime;

        //Receive message
        while (currentTime - initialTime <= 30000 && message.equals("")) { //Control time between messages
            message = in.readLine(); //Reads the message with the route from the buffer
            currentTime = System.currentTimeMillis();
        }

        //If there is no message in that time the connection is lost, else the routes are updated
        if (message == null) {
            this.timeout();
        } else {
            if (as_ID == -1){
                as_ID = manager.getASIDFromMessage(message);
                System.err.println("AS"+as_ID+" is now connected.");
            }
            manager.updateRoutes(message);
            //Send message
            String temporal = manager.routesToString();
            out.println(manager.routesToString(as_ID)); //Writes the updated routes to the buffer
        }
    }

    /**
     * Closes all the buffers used in the communication and close the mini_server socket
     */
    void kill() {

        if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException e) {
                System.err.println("AS"+as_ID+" connection couldn't be closed (input error)");
                log_file.println("AS"+as_ID+" connection couldn't be closed (input error).");
            }
        }

        if (this.out != null) {
            this.out.close();
        }

        if (this.mini_Server != null) {
            try {
                this.mini_Server.close();
            } catch (IOException e) {
                System.err.println("AS"+as_ID+" connection couldn't be closed (client socket error).");
                log_file.println("AS"+as_ID+" connection couldn't be closed (client socket error).");
            }
        }

        this.isOn = false;

    }

    /**
     * Verifies the AS of the lost connection and and removes all the routes which include it
     */
    private void timeout () {
        System.err.println("AS" + as_ID + " has timed out.");
        log_file.println("AS" + as_ID + " has timed out.");
        if (as_ID != -1) {
            manager.removeRoutesFromAS(as_ID);
        }
        kill();
    }


    /**
     * Server thread's function, receives a message each 30s from its neighbours and respond to it
     */
    @Override
    public void run(){
        this.isOn = true;

        //this.as.depositMessage("Server with AS?? started.");
        while (this.isOn) {
            try {
                this.manageMessages();
            } catch (IOException e) {
                this.timeout();
            } catch (NullPointerException e) {
                this.timeout();
            }

            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                //Nothing should happen
            }
        }

    }
}
