
import Server_Aux.Server;
import Server_Aux.Server_Handler;
import lombok.val;

import java.io.*;
import java.util.*;

import lombok.extern.java.Log;

/**
 * Created by luka on 6.7.17..
 *
 * Adapted by   Carlos Gamboa Vargas
 *              Fernando Rojas Meléndez
 *              Ana Laura Vargas Ramírez
 *
 */
@Log
public class ServerApplication {

    private static int port;
    private static Map<String, String> neighbours;
    private static ArrayList<String> networks;

    private static void loadFile (String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            int type = 0;

            while (line != null) {
                if (line.equals("#Redes conocidas")){
                    type = 1;
                }
                else if (line.equals("#Vecinos BGP")) {
                    type = 2;
                }
                else if (line.equals("#Escuchar vecinos")) {
                    type = 3;
                }
                else {
                    if(type == 1){
                        networks.add(line);
                    } else if (type == 2) {
                        StringTokenizer tokens = new StringTokenizer(line, ":");
                        String key = tokens.nextToken();
                        String value = tokens.nextToken();
                        neighbours.put(key, value);
                    }
                    else if (type == 3) {
                        port = Integer.parseInt(line);
                    }
                }
                line = br.readLine();
            }
            br.close();
        }
        catch (IOException e) {
            System.out.print("Could not find the file.\nEnter AS information filename: ");
        }
    }

    private static void start (Scanner sc) {
        System.out.print("Enter AS information filename: ");
        String file = sc.nextLine();
        loadFile(file);
    }

    public static void main(String[] args) {
        networks = new ArrayList<>();
        neighbours = new HashMap<>();

        val sc = new Scanner(System.in);
        log.info("The Server is running.");
        System.out.print("> ");
        String command = "";
        do {
            command = sc.nextLine();
            //TODO: Manage other commands.
        }while (!command.equals("start"));

        log.info("The Server has started.");
        start(sc);

        val server = new Server(port, 250);
        server.listen(new Server_Handler(networks, neighbours));
    }
}
