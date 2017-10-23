package Server_Aux;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.val;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.EOFException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * Created by luka on 6.7.17..
 *
 * Adapted by   Carlos Gamboa Vargas
 *              Fernando Rojas Meléndez
 *              Ana Laura Vargas Ramírez
 *
 */
@Log
public class Server_Handler implements OnConnectListener {

    private ArrayList<Client> devices;
    private Map<String, String> neighbours;
    private ArrayList<String> networks;
    ListMultimap<String, String> routesMultimap;
    private Boolean on;

    /**
     * Creates a Hub Handler to manage the messages.
     *
     * @param neighbours
     * @param networks
     */
    public Server_Handler(@NonNull ArrayList<String> networks, Map<String, String> neighbours) {
        devices = new ArrayList<>();
        this.neighbours = neighbours;
        this.networks = networks;
        this.on = true;
        this.routesMultimap = ArrayListMultimap.create();
        startTerminal();
    }

    private void startTerminal () {
        val sc = new Scanner(System.in);
        System.out.print("> ");
        String line = "";
        while (true) {
            line = sc.nextLine();
            StringTokenizer tokens = new StringTokenizer(line, "<");
            String command = tokens.nextToken();
            String network = "";
            if (tokens.hasMoreTokens()){
                network = tokens.nextToken();
                network = network.replace(">", "");
            }
            if (command.equals("stop")) {
                stop();
            }
            else if (command.equals("add")) {
                add(network);
            }
            else if (command.equals("show routes")){
                show_Route();
            }
            else if (command.equals("start")){
                start();
            }
        }
    }

    private void stop() {
        if (isOn()) {
            this.on = false;
            log.info("The server has stopped");
        }
    }

    private void add (String network) {
        if (isOn()) {
            networks.add(network);
        }
    }

    private void show_Route() {
        if (isOn()) {

        }
    }

    private void start (){
        if (!isOn()) {
            this.on = true;
            log.info("The server has started");
        }
    }

    private Boolean isOn(){
        return this.on;
    }

    private void readMessage (String message){
        StringTokenizer tokens = new StringTokenizer(message, "*");
        String identifier = tokens.nextToken();
        String information = tokens.nextToken();
        tokens = new StringTokenizer(information, ":");
        String ip = tokens.nextToken();
        String routes = tokens.nextToken();
        routesMultimap.removeAll(ip);
        tokens = new StringTokenizer(routes, ",");
        String route = "";
        while (tokens.hasMoreTokens()) {
            route = tokens.nextToken();
            routesMultimap.put(ip, route);
        }
    }

    /**
     * Overrides onConnect method.
     *
     * @param client Device you want to connect.
     */
    @Override
    public void onConnect(@NonNull Client client) {
        devices.add(client);
        startClientThread(client);
    }

    /**
     * Checks if a Device is in the cluster. If it is not, it can add it to the cluster or reject it.
     *
     * @param message Message received by the Hub.
     * @return true if the device is part of the custer | false if the device is not part of the cluster.
     */
    private boolean associateDevicePublicKey(String message) {
        /*String name = getDeviceName(message);

        if (!device_keys.containsKey(name)) {
            val sc = new Scanner(System.in);
            log.warning("Received a message from " + name + ", who's not in your cluster.");
            System.out.print("Insert " + name + "'s public key filename. If you don't want to add this device to the cluster, insert \'No\'\n");
            val publicKeyFilename = sc.nextLine();
            val publicKeyLocation = device_keys_path + publicKeyFilename;
            if (!publicKeyFilename.equals("No")) {
                val devicesPublicKey = readPublicKeyFromFile(publicKeyLocation);
                if (devicesPublicKey == null) {
                    return false;
                }
                device_keys.put(name, devicesPublicKey);
                return true;
            }
            else {
                System.out.print(name + " was not added to the cluster.");
                return false;
            }
        }
        else {
            return true;
        }*/
        return true;
    }

    /**
     * Starts a new Device thread. This means, that listens to new messages from devices.
     *
     * @param client The Device you want to start.
     */
    private void startClientThread(@NonNull Client client) {
        new Thread(() -> {
            while (true) {
                try {
                    val bytesSentByClient = (byte[]) client.getObjectInputStream().readObject();
                    showMessage(bytesSentByClient);
                } catch (EOFException ex) {
                    log.warning("Client closed connection");
                    break;

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Broadcasts a message to all devices.
     *
     * @param bytes Message you want to broadcast.
     */
    private void broadcast(@NonNull byte[] bytes) {
        devices.forEach(device -> {
            try {
                device.getObjectOutputStream().writeObject(bytes);
            } catch (IOException e) {
                log.warning("Cannot send bytes to client: " + device.getSocket().getInetAddress() + ":" + device.getSocket().getPort());
                e.printStackTrace();
            }
        });
    }

    /**
     * Displays the received message.
     *
     * @param encryptedMessage Received message.
     */
    private void showMessage (@NonNull byte[] encryptedMessage) {
        /*try {
            val decryptedMessage = EncryptionUtil.decrypt(encryptedMessage, myPrivateKey);
            Boolean added = associateDevicePublicKey(decryptedMessage);
            if (added) {
                synchronized (System.out) {
                    System.out.print(decryptedMessage);
                }
                sendResponseMessage(getDeviceName(decryptedMessage));
                checkMessageKnown(decryptedMessage);
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ignored) {

        }*/
    }

    /**
     * Gets the name of a device based on the message they send.
     *
     * @param message Message received by the hub.
     * @return Devices name.
     */
    private String getDeviceName(String message) {
        StringTokenizer tokens = new StringTokenizer(message, ":");
        return tokens.nextToken();
    }

    /**
     * Encrypts a response message
     *
     * @param message Message to be encrypted.
     * @param device_Name Name of the device you want to send the encrypted message.
     * @return Encrypted message.
     */
    private byte[] encryptResponseMessage (String message, String device_Name) {
        //return EncryptionUtil.encrypt("Hub: " + message, device_keys.get(device_Name));
        return new byte[5];
    }

    /**
     * Sends a message.
     * @param device_Name Name of the device you want to send the message.
     */
    private void sendResponseMessage (String device_Name) {
        byte[] responseMessage = encryptResponseMessage("Message received", device_Name);
        broadcast(responseMessage);
    }

    /**
     * Checks if the message is a command.
     *
     * @param message Message received.
     */
    private void checkMessageKnown (String message) {
        StringTokenizer tokens = new StringTokenizer(message, ":");
        String name = tokens.nextToken();
        String full_message = tokens.nextToken();
        String data = "";
        StringTokenizer command_Tokens = new StringTokenizer(full_message, "!");
        String command = command_Tokens.nextToken();
        if (command_Tokens.hasMoreTokens()) {
             data = command_Tokens.nextToken();
        }
        if (command.equals(" Send") && !data.equals("")) {
            String final_Message = name + " sends the following data:" + data;
            byte[] responseMessage = encryptResponseMessage(final_Message, getRandomDeviceName(name));
            broadcast(responseMessage);
        }
    }

    /**
     * Gets a random device name.
     *
     * @param deviceName The name of the sender device.
     * @return String with the random device name.
     */
    private String getRandomDeviceName(String deviceName) {
        /*Random randomGenerator = new Random();
        List<String> keys = new ArrayList<String>(device_keys.keySet());
        String randomKey;
        do {
            randomKey = keys.get(randomGenerator.nextInt(keys.size()) );
        } while (deviceName.equals(randomKey));
        return randomKey; */
        return "hole";
    }
}