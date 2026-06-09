package client;

import common.ClientInterface;
import common.Point;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import raytracer.Image;
import raytracer.Scene;

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
    public List<Image> compute(Scene scene, Point point, int width, int height) throws RemoteException {
        int sw = width / 2;
        int sh = height / 2;

        // Coins haut-gauche de chaque sous-tuile : HG, HD, BG, BD
        int[][] offsets = {
            {0,  0 },
            {sw, 0 },
            {0,  sh},
            {sw, sh}
        };

        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<Image>> futures = new ArrayList<>(4);

        for (int[] off : offsets) {
            int ax = point.x + off[0];
            int ay = point.y + off[1];
            futures.add(pool.submit(() -> {
                System.out.println(name + " [" + Thread.currentThread().getName()
                        + "] calcule sous-tuile (" + ax + "," + ay + ") taille " + sw + "x" + sh);
                return scene.compute(ax, ay, sw, sh);
            }));
        }
        pool.shutdown();

        List<Image> results = new ArrayList<>(4);
        try {
            for (Future<Image> f : futures) {
                results.add(f.get()); // bloque jusqu'à ce que la sous-tuile soit prête
            }
        } catch (Exception e) {
            throw new RemoteException("Erreur calcul sous-tuile : " + e.getMessage(), e);
        }
        return results;
    }
}