package application;
 
import java.util.ArrayList;
import java.util.Comparator;
 
public class GestionnaireTaches {
    private ArrayList<Tache> taches = new ArrayList<>();
 
    public void ajouter(Tache tache) {
        taches.add(tache);
    }
 
    public void supprimer(Tache tache) {
        taches.remove(tache);
    }
 
    public ArrayList<Tache> getTaches() {
        return taches;
    }
 
    // Renvoie une nouvelle liste contenant seulement les tâches voulues
    public ArrayList<Tache> filtrer(Filtre filtre) {
        ArrayList<Tache> resultat = new ArrayList<>();
        for (Tache t : taches) {
            if (filtre == Filtre.TOUTES
                    || (filtre == Filtre.EN_COURS && !t.isTerminee())
                    || (filtre == Filtre.TERMINEES && t.isTerminee())) {
                resultat.add(t);
            }
        }
        return resultat;
    }
 
    // Trie la liste de la date la plus proche à la plus lointaine
    public void trierParDate() {
        taches.sort(Comparator.comparing(Tache::getDate));
    }
}