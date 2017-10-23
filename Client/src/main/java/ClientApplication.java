import Client_Aux.Client;
import Client_Aux.Client_Handler;
import lombok.NonNull;
import lombok.val;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by luka on 6.7.17..
 *
 * Adapted by   Carlos Gamboa Vargas
 *              Fernando Rojas Meléndez
 *              Ana Laura Vargas Ramírez
 *
 */
public class ClientApplication {


    private static String[] genericNames;
    private static Random randomGenerator;
    private static String device_key_path = "C:\\Users\\Dell\\Documents\\Universidad\\Redes de Computadores\\SH_RSA_Socket\\Device\\Device_Keys\\";
    private static String hub_key_path = "C:\\Users\\Dell\\Documents\\Universidad\\Redes de Computadores\\SH_RSA_Socket\\Hub\\Hub_Keys\\";

    /**
     * Reads a public key from a file
     *
     * @param path Path to the public key file/
     * @return PublicKey|null
     */
    private static PublicKey readPublicKeyFromFile(@NonNull String path) {
        try {
            return (PublicKey) new ObjectInputStream(new FileInputStream(path)).readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.print("Could not find the public key file.\nInsert hub's public key filename.\n");
        }

        return null;
    }

    /**
     * Reads a private key from a file
     *
     * @param path Path to the private key file/
     * @return PrivateKey|null
     */
    private static PrivateKey readPrivateKeyFromFile(@NonNull String path) {
        try {
            return (PrivateKey) new ObjectInputStream(new FileInputStream(path)).readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.print("Could not find the private key file.\nInsert device private key filename.\n");
        }

        return null;

    }

    private static String generateRandomUsername () {
        String sensorName = genericNames[randomGenerator.nextInt(genericNames.length)];
        sensorName = sensorName + "-" + String.valueOf(randomGenerator.nextInt(200));
        return sensorName;
    }

    public static void main(String args[]) throws Exception {
        genericNames = new String[]{"MCE", "WTR", "IS", "IR", "HR"};
        randomGenerator = new Random();

        val sc = new Scanner(System.in);

        //System.out.print("Hostname: ");
        //val hostname = sc.nextLine();

        val hostname = "localhost";

        //System.out.print("Port: ");
        //val port = sc.nextInt();
        //sc.nextLine();
        val port = 8080;

        //System.out.print("Username: ");
        //val username = sc.nextLine();
        val username = generateRandomUsername();

        System.out.print("Device private key filename: ");
        PrivateKey myPrivateKey = null;
        do {
            val privateKeyLocation = device_key_path + sc.nextLine();
            myPrivateKey = readPrivateKeyFromFile(privateKeyLocation);
        }while (myPrivateKey == null);


        System.out.print("Hub's public key filename: ");
        PublicKey hub_public_key = null;
        do {
            val publicKeyLocation = hub_key_path + sc.nextLine();
            hub_public_key = readPublicKeyFromFile(publicKeyLocation);
        }while (hub_public_key == null);

        val client = new Client(hostname, port);
        client.connectToServer(new Client_Handler(myPrivateKey));

        //noinspection InfiniteLoopStatement
        while (true) {
            synchronized (System.in) {
                String rawText = username + ": " + sc.nextLine();

                val encryptedText = EncryptionUtil.encrypt(rawText, hub_public_key);
                client.getServer().getObjectOutputStream().writeObject(encryptedText);
            }
        }
    }
}
