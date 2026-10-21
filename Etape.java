package application;
 
public class Etape {
    private String titre;
    private boolean faite;
 
    public Etape(String titre) {
        this.titre = titre;
        this.faite = false;
    }
 
    public String getTitre() { return titre; }
    public boolean isFaite() { return faite; }
    public void setFaite(boolean faite) { this.faite = faite; }
 
    @Override
    public String toString() {
        return (faite ? "[x] " : "[ ] ") + titre;
    }
}
 
