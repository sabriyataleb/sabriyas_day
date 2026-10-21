package application;
 
import java.time.LocalDate;
import java.util.ArrayList;
 
public class Tache {
    private String titre;
    private LocalDate date;
    private boolean terminee;
    private ArrayList<Etape> etapes = new ArrayList<>();
 
    public Tache(String titre, LocalDate date) {
        this.titre = titre;
        this.date = date;
        this.terminee = false;
    }
 
    public String getTitre() { return titre; }
    public LocalDate getDate() { return date; }
    public boolean isTerminee() { return terminee; }
    public ArrayList<Etape> getEtapes() { return etapes; }
 
    public void setTitre(String titre) { this.titre = titre; }
    public void setDate(LocalDate date) { this.date = date; }
 
    // Ajoute une étape : la tâche n'est plus terminée puisqu'il reste quelque chose à faire
    public void ajouterEtape(String titreEtape) {
        etapes.add(new Etape(titreEtape));
        terminee = false;
    }
 
    // Coche ou décoche une étape, puis met à jour l'état de la tâche
    public void setEtapeFaite(int index, boolean faite) {
        etapes.get(index).setFaite(faite);
        terminee = nbEtapesFaites() == etapes.size();
    }
 
    // Valide directement toute la tâche : toutes les étapes sont cochées
    public void valider() {
        for (Etape e : etapes) {
            e.setFaite(true);
        }
        terminee = true;
    }
 
    private int nbEtapesFaites() {
        int nb = 0;
        for (Etape e : etapes) {
            if (e.isFaite()) {
                nb++;
            }
        }
        return nb;
    }
 
    // Avancement en pourcentage (0 à 100)
    public int getProgression() {
        if (etapes.isEmpty()) {
            return terminee ? 100 : 0;
        }
        return nbEtapesFaites() * 100 / etapes.size();
    }
 
    @Override
    public String toString() {
        String etat = terminee ? "[x] " : "[ ] ";
        return etat + titre + " (" + date + ") - " + getProgression() + "%";
    }
}