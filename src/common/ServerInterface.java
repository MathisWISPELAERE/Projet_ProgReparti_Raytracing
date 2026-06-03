package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    // point d'entrée des clients pour qu'ils soient utilisé pour les calculs 
    void registerClient(ClientInterface client) throws RemoteException;
}
