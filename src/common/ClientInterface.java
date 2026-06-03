package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import raytracer.Image;
import raytracer.Scene;

public interface ClientInterface extends Remote {

    String getName() throws RemoteException;

    /**
     * Demande au client de calculer une case de l'image.
     *
     * @param scene  la scène à rendre (sérialisable)
     * @param point  coin haut-gauche de la tuile
     * @param width  largeur de la tuile
     * @param height hauteur de la tuile
     * @return l'image calculée pour cette tuile
     */
    Image compute(Scene scene, Point point, int width, int height) throws RemoteException;
}
