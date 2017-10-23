package Client_Aux;

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
import java.security.PrivateKey;

/**
 * Created by luka on 6.7.17..
 *
 * Adapted by   Carlos Gamboa Vargas
 *              Fernando Rojas Meléndez
 *              Ana Laura Vargas Ramírez
 *
 */
@Log
public class Client_Handler implements OnConnectToServer {

    private PrivateKey myPrivateKey;

    /**
     * Creates a Device Handler to manage the messages.
     *
     * @param myPrivateKey Device's private key.
     */
    public Client_Handler(@NonNull PrivateKey myPrivateKey) {
        this.myPrivateKey = myPrivateKey;
    }

    /**
     * Overrides the onConnectToServer method
     *
     * @param server The cluster's hub.
     */
    @Override
    public void onConnectToServer(@NonNull Server server) {
        startListenerThread(server);
    }

    /**
     * Listens to the hub in case there are messages.
     *
     * @param server Cluster's hub.
     */
    private void startListenerThread(@NonNull Server server) {
        new Thread(() -> {
            while (true) {
                try {
                    val receivedEncryptedMessage = (byte[]) server.getObjectInputStream().readObject();
                    try {
                        val decryptedMessage = EncryptionUtil.decrypt(receivedEncryptedMessage, myPrivateKey);
                        synchronized (System.out) {
                            System.out.println(decryptedMessage);
                        }
                    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ignored) {

                    }

                } catch (EOFException ex) {
                    log.severe("Hub closed connection");
                    break;
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();

                }
            }
        }).start();
    }
}