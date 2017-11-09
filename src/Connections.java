import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connections extends Thread {

    private Socket mini_Server;
    private boolean isOn;
    private Routes_Manager manager;

    private BufferedReader in;
    private PrintWriter out;

    Connections (Socket clientSocket, Routes_Manager manager) {
        this.mini_Server = clientSocket;
        this.manager = manager;
    }


    private void manageMessages() throws IOException {

        if (this.in == null) {
            this.in = new BufferedReader(new InputStreamReader(mini_Server.getInputStream()));
        }
        if (this.out == null) {
            this.out = new PrintWriter(mini_Server.getOutputStream(), true);
        }

        String message = "";

        long initialTime = System.currentTimeMillis();
        long currentTime = initialTime;

        while (currentTime - initialTime <= 30000 && message == null) {
            message = in.readLine();
            currentTime = System.currentTimeMillis();
        }

        if (message == null) {
            //this.timeout();
        } else {
            manager.updateRoutes(message);
            out.println(manager.routesToString());
        }
    }

    void kill() {

        if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException e) {
                //Do nothing
            }
        }

        if (this.out != null) {
            this.out.close();
        }

        if (this.mini_Server != null) {
            try {
                this.mini_Server.close();
            } catch (IOException e) {
                //Do nothing
            }
        }

        this.isOn = false;

    }

    private void finishConnection () {

        /*this.as.depositMessage("Client of " + (this.neighborAsId.equals("")? "AS??":this.neighborAsId) + " didn't respond, finishing connection");
        this.as.deleteAllRoutesWithAS(this.neighborAsId);
        this.as.depositMessage("All routes with " + (this.neighborAsId.equals("")? "AS??":this.neighborAsId) + " have been deleted.");*/
        this.kill();

    }

    @Override
    public void run() {
        this.isOn = true;

        //this.as.depositMessage("Server with AS?? started.");
        while (this.isOn) {
            try {
                this.manageMessages();
            } catch (IOException e) {
                this.finishConnection();
            }

            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                //Nothing should happen
            }
        }

    }
}
