package client;

import common.ClientInterface;
import common.ServerInterface;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientMain {

    public static void main(String[] args) {
        try {
             // On récupère le nom/IP de la machine locale pour identifier ce client
            // Si on passe un argument en ligne de commande on l'utilise à la place
            if (args.length == 0) {
                System.out.println("Lancer le client avec au moins un argument :\n\t - ClientMain IPServer {NomClient}");
            }
            InetAddress localhost = InetAddress.getLocalHost();
            String clientName = args.length > 1 ? args[1] : ""+localhost.getHostName()+" : "+localhost.getHostAddress();
            Registry registry = LocateRegistry.getRegistry(args[0], 1099);
            ServerInterface server = (ServerInterface) registry.lookup("ServeurRaytracing");

            ClientInterface client = new ClientImpl(clientName);
            server.registerClient(client);

            System.out.println(clientName + " Serveur trouvé. En attente de travail...");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
