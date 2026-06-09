package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import raytracer.Image;
import raytracer.Scene;

public interface ClientInterface extends Remote {

    String getName() throws RemoteException;

    /**
     * Demande au client de calculer une case de l'image.
     * Le client subdivise la tuile reçue en 4 sous-tuiles égales,
     * calcule chacune et retourne la liste dans l'ordre :
     * haut-gauche, haut-droit, bas-gauche, bas-droit.
     *
     * @param scene  la scène à rendre (sérialisable)
     * @param point  coin haut-gauche de la tuile
     * @param width  largeur de la tuile
     * @param height hauteur de la tuile
     * @return liste de 4 images (sous-tuiles) calculées par ce client
     */
    List<Image> compute(Scene scene, Point point, int width, int height) throws RemoteException;
}