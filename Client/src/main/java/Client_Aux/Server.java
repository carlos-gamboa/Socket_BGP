package Client_Aux;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by luka on 6.7.17..
 *
 * Adapted by   Carlos Gamboa Vargas
 *              Fernando Rojas Meléndez
 *              Ana Laura Vargas Ramírez
 *
 */
@SuppressWarnings("WeakerAccess")
@Data
@Builder
public class Server {

    private Socket socket;

    @Getter
    private ObjectOutputStream objectOutputStream;

    @Getter
    private ObjectInputStream objectInputStream;

}
