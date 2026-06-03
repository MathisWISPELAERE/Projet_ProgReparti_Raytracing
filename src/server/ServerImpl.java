package server;

import common.ClientInterface;
import common.ServerInterface;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ServerImpl extends UnicastRemoteObject implements ServerInterface {

    // listes des clients qui effectueront les calculs
    private final List<ClientInterface> clients = new ArrayList<>();

    public ServerImpl() throws RemoteException {
        super();
    }

    // synchorinzed pour éviter les problèmes d'enregistrement de plusieurs client EN MEME TEMPS même si en test ça arrivera jamais
    @Override
    public synchronized void registerClient(ClientInterface client) throws RemoteException {
        clients.add(client);
        System.out.println("Client connecté : " + client.getName() + " (total : " + clients.size() + ")");
    }

    // synchronized pas obligatoire vu qu'aucune modification interne n'en découle mais on sait jamais
    public synchronized List<ClientInterface> getClients() {
        return new ArrayList<>(clients);
    }
}
