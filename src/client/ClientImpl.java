package client;

import common.ClientInterface;
import common.Point;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import raytracer.Image;
import raytracer.Scene;

// "extends UnicastRemoteObjet" est pas obligatoire mais ça évite de le faire à la création du service
public class ClientImpl extends UnicastRemoteObject implements ClientInterface {

    private final String name;

    public ClientImpl(String name) throws RemoteException {
        super();
        this.name = name;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public Image compute(Scene scene, Point point, int width, int height) throws RemoteException {
        System.out.println(name + " calcule tuile (" + point.x + "," + point.y + ") taille " + width + "x" + height);
        return scene.compute(point.x, point.y, width, height);
    }
}
