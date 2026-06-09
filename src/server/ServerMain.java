package server;

import common.ClientInterface;
import common.Point;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import raytracer.Disp;
import raytracer.Image;
import raytracer.Scene;

public class ServerMain {

    /**
     * Calcule la taille d'une case en fonction des dimensions de l'image.
     *
     * plus petite dimension/8 puis on arrondie à la puissance de 2 la plus proche, bornée entre 32 et 256 px.
     * comme ça on évite de faire des trop petit carré pour les grosses dimension et de trop gros carré pour les petites
     */
    private static int calculerTailleCase(int largeur, int hauteur) {
        int petiteCote = Math.min(largeur, hauteur);
        int taille = petiteCote / 8;
 
        // Arrondir à la puissance de 2 la plus proche
        if (taille > 0) {
            int inf = Integer.highestOneBit(taille);
            int sup = inf << 1;
            taille = (taille - inf < sup - taille) ? inf : sup;
        }
 
        // Borner entre 32 et 256 px
        return Math.max(32, Math.min(256, taille));
    }

    public static void main(String[] args) throws Exception {

        Registry reg = LocateRegistry.createRegistry(1099);
        ServerImpl server = new ServerImpl();
        reg.rebind("ServeurRaytracing", server);
        System.out.println("Serveur prêt.");

        String fichierDescription = args.length > 0 ? args[0] : "simple.txt";
        int largeur = args.length > 1 ? Integer.parseInt(args[1]) : 512; 
        int hauteur = args.length > 2 ? Integer.parseInt(args[2]) : 512;

        Disp disp = new Disp("Raytracer", largeur, hauteur);
        Scene scene = new Scene(fichierDescription, largeur, hauteur);

        // --- Attente des clients ---
        // On attend que l'utilisateur appuie sur Entrée pour laisser le temps 
        // aux clients de se connecter avant de démarrer le rendu
        Scanner sc = new Scanner(System.in);
        System.out.println("En attente des clients. Appuyez sur Entrée pour lancer le rendu...");
        sc.nextLine();

        List<ClientInterface> clients = server.getClients();
        if (clients.isEmpty()) {
            System.out.println("Aucun client connecté. Abandon.");
            return;
        }
        System.out.println(clients.size() + " client(s) connecté(s).");

        int tailleCase = calculerTailleCase(largeur, hauteur);

        // --- Construction de la file des cases ---
        int tw = largeur / tailleCase;   // largeur d'une case
        int th = hauteur / tailleCase;   // hauteur d'une case

        // ConcurrentLinkedQueue = une file 
        // Plusieurs threads peuvent faire poll() en même temps sans se marcher dessus
        // J'ai d'abord essayé avec une ArrayList normale mais ça causait des problèmes
        // quand plusieurs threads modifiaientt en même temps
        ConcurrentLinkedQueue<Point> cases = new ConcurrentLinkedQueue<>();
        for (int x = 0; x < largeur; x += tw) {
            for (int y = 0; y < hauteur; y += th) {
                cases.add(new Point(x, y));
            }
        }
        System.out.println(cases.size() + " tuiles à calculer (" + tw + "x" + th + " px chacune).");

        // --- Distribution : un thread par client ---
        int nbCases = cases.size();
        // AtomicInteger = un entier qu'on peut incrémenter depuis plusieurs threads
        // sans risque de perdre des mises à jour
        // Si deux threads font fini++ en même temps sur un int normal, une des
        // deux incrémentations peut être "perdue" — AtomicInteger règle ça
        AtomicInteger fini = new AtomicInteger(0); // Donc permet d'être utilisé par plusieur thread en même temps
        // ExecutorService gère un pool de threads — ici un thread par client
        // C'est mieux que de créer des Thread à la main, selon la doc et l'IA, c'est
        // la façon recommandée de gérer des threads en Java moderne
        ExecutorService pool = Executors.newFixedThreadPool(clients.size());

        Instant debut = Instant.now();

        // Pour chaque client on soumet une tâche a l'ExecutorService
        // Chaque tâche boucle et prend des cases dans la file tant qu'il en reste
        for (ClientInterface client : clients) {
            pool.submit(() -> {
                Point cas;
                while ((cas = cases.poll()) != null) {
                    Point t = cas;
                    try {
                        List<Image> imgs = client.compute(scene, t, tw, th);

                        // Les 4 sous-tuiles sont dans l'ordre HG, HD, BG, BD
                        int sw = tw / 2;
                        int sh = th / 2;
                        int[][] offsets = {{0, 0}, {sw, 0}, {0, sh}, {sw, sh}};

                        synchronized (disp) {
                            for (int i = 0; i < imgs.size(); i++) {
                                disp.setImage(imgs.get(i), t.x + offsets[i][0], t.y + offsets[i][1]);
                            }
                        }

                        int n = fini.incrementAndGet();
                        String nomThread = Thread.currentThread().getName();
                        System.out.println("[" + nomThread + "] Tuile (" + t.x + "," + t.y + ") reçue ["+ n + "/" + nbCases + "]");

                    } catch (Exception e) {
                        System.err.println("Erreur client sur tuile ("
                                + t.x + "," + t.y + ") : " + e.getMessage());
                        // Si le client a planté on remet la tuile dans la file
                        // pour qu'un autre client puisse la traiter
                        cases.add(t);
                    }
                }
            });
        }
        // shutdown() = "plus de nouvelles tâches"
        // permet un arrêt "propre" du pool
        pool.shutdown();
        // ici attend la fin de toutes les tâches
        pool.awaitTermination(1, TimeUnit.HOURS);

        long duree = Duration.between(debut, Instant.now()).toMillis();
        System.out.println("Rendu terminé en " + duree + " ms.");
    }
}