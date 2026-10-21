package application;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class AppTaches extends Application {

    private static final DateTimeFormatter FORMAT_COURT =
            DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH);
    private static final DateTimeFormatter FORMAT_LONG =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    // Données
    private GestionnaireTaches gestionnaire = new GestionnaireTaches();
    private ObservableList<Tache> affichees = FXCollections.observableArrayList();
    private Filtre filtreCourant = Filtre.EN_COURS;
    private Tache courante = null;

    // Panneau de gauche
    private ListView<Tache> liste = new ListView<>(affichees);
    private Label statEnCours = new Label();
    private Label statTerminees = new Label();
    private Label statMoyenne = new Label();

    // Panneau de droite : détail
    private VBox boiteDetail = new VBox(8);
    private Label detailTitre = new Label();
    private Label detailDate = new Label();
    private VBox boiteEtapes = new VBox(6);
    private TextField champEtape = new TextField();

    // Panneau de droite : nouvelle tâche
    private TextField champTitre = new TextField();
    private DatePicker champDate = new DatePicker(LocalDate.now());
    private Label messageErreur = new Label();

    @Override
    public void start(Stage stage) {
        donneesExemple();

        BorderPane racine = new BorderPane();
        racine.setTop(creerEntete());
        racine.setCenter(creerPanneauGauche());
        racine.setRight(creerPanneauDroit());

        Scene scene = new Scene(racine, 900, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("Sabriya’s Day");
        stage.setScene(scene);
        stage.show();

        rafraichir();
    }

    // ---------- Construction de l'interface ----------

    private HBox creerEntete() {
        Label titre = new Label("Sabriya’s Day");
        titre.getStyleClass().add("titre-app");

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        String jour = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH));
        Label date = new Label(jour);
        date.getStyleClass().add("date-jour");

        HBox entete = new HBox(10, creerNoeud(), titre, espace, date);
        entete.setAlignment(Pos.CENTER_LEFT);
        entete.getStyleClass().add("entete");
        return entete;
    }

    // Le noeud rose, dessiné avec des formes
    private Group creerNoeud() {
        SVGPath boucleGauche = forme("M22 16 C14 3 3 2 3 11 C3 20 14 21 22 16 Z", "#ED93B1");
        SVGPath boucleDroite = forme("M22 16 C30 3 41 2 41 11 C41 20 30 21 22 16 Z", "#ED93B1");
        SVGPath rubanGauche = forme("M20 18 L12 30 L19 27 L22 20 Z", "#D4537E");
        SVGPath rubanDroit = forme("M24 18 L32 30 L25 27 L22 20 Z", "#D4537E");

        Ellipse centre = new Ellipse(22, 16, 4.5, 5);
        centre.setFill(Color.web("#D4537E"));
        centre.setStroke(Color.web("#993556"));

        return new Group(boucleGauche, boucleDroite, rubanGauche, rubanDroit, centre);
    }

    private SVGPath forme(String chemin, String couleur) {
        SVGPath p = new SVGPath();
        p.setContent(chemin);
        p.setFill(Color.web(couleur));
        p.setStroke(Color.web("#993556"));
        return p;
    }

    private VBox creerPanneauGauche() {
        // Statistiques
        HBox stats = new HBox(8,
                creerStat(statEnCours, "en cours"),
                creerStat(statTerminees, "terminées"),
                creerStat(statMoyenne, "avancement moyen"));

        // Filtres
        ToggleGroup groupe = new ToggleGroup();
        ToggleButton toutes = creerFiltre("Toutes", Filtre.TOUTES, groupe);
        ToggleButton enCours = creerFiltre("En cours", Filtre.EN_COURS, groupe);
        ToggleButton terminees = creerFiltre("Terminées", Filtre.TERMINEES, groupe);
        enCours.setSelected(true);

        // Empêche de désélectionner tous les filtres
        groupe.selectedToggleProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau == null) {
                ancien.setSelected(true);
            }
        });

        Button trier = new Button("Trier par date");
        trier.getStyleClass().add("bouton");
        trier.setOnAction(e -> {
            gestionnaire.trierParDate();
            rafraichir();
        });

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);
        HBox filtres = new HBox(6, toutes, enCours, terminees, espace, trier);
        filtres.setAlignment(Pos.CENTER_LEFT);

        // Liste : chaque ligne est une "carte" avec titre, date, pourcentage et barre
        liste.setCellFactory(lv -> new ListCell<Tache>() {
            @Override
            protected void updateItem(Tache t, boolean vide) {
                super.updateItem(t, vide);
                if (vide || t == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label nom = new Label(t.getTitre());
                nom.getStyleClass().add("nom-tache");
                if (t.isTerminee()) {
                    nom.getStyleClass().add("nom-fini");
                }

                Label date = new Label(t.getDate().format(FORMAT_COURT));
                date.getStyleClass().add("date");

                Label pct = new Label(t.getProgression() + "%");
                pct.getStyleClass().add("pct");

                Region espace = new Region();
                HBox.setHgrow(espace, Priority.ALWAYS);
                HBox ligne = new HBox(8, nom, espace, date, pct);
                ligne.setAlignment(Pos.CENTER_LEFT);

                ProgressBar barre = new ProgressBar(t.getProgression() / 100.0);
                barre.setMaxWidth(Double.MAX_VALUE);
                if (t.isTerminee()) {
                    barre.getStyleClass().add("barre-ok");
                }

                VBox carte = new VBox(6, ligne, barre);
                carte.getStyleClass().add("carte");
                carte.prefWidthProperty().bind(liste.widthProperty().subtract(32));

                setText(null);
                setGraphic(carte);
            }
        });

        liste.getSelectionModel().selectedItemProperty()
                .addListener((obs, ancien, nouveau) -> afficherDetail(nouveau));
        VBox.setVgrow(liste, Priority.ALWAYS);

        VBox gauche = new VBox(12, stats, filtres, liste);
        gauche.setPadding(new Insets(14));
        return gauche;
    }

    private VBox creerStat(Label nombre, String texte) {
        nombre.getStyleClass().add("stat-nombre");
        Label legende = new Label(texte);
        legende.getStyleClass().add("stat-texte");

        VBox boite = new VBox(nombre, legende);
        boite.getStyleClass().add("stat-boite");
        boite.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(boite, Priority.ALWAYS);
        return boite;
    }

    private ToggleButton creerFiltre(String texte, Filtre filtre, ToggleGroup groupe) {
        ToggleButton b = new ToggleButton(texte);
        b.setToggleGroup(groupe);
        b.getStyleClass().add("filtre");
        b.setOnAction(e -> {
            filtreCourant = filtre;
            rafraichir();
        });
        return b;
    }

    private VBox creerPanneauDroit() {
        // --- Détail de la tâche sélectionnée ---
        Label sousTitreDetail = new Label("Détail de la tâche");
        sousTitreDetail.getStyleClass().add("sous-titre");
        detailTitre.getStyleClass().add("detail-titre");
        detailDate.getStyleClass().add("date");

        champEtape.setPromptText("Nouvelle étape");
        champEtape.setOnAction(e -> ajouterEtape());
        HBox.setHgrow(champEtape, Priority.ALWAYS);
        Button boutonEtape = new Button("Ajouter");
        boutonEtape.getStyleClass().add("bouton");
        boutonEtape.setOnAction(e -> ajouterEtape());
        HBox ligneEtape = new HBox(8, champEtape, boutonEtape);

        Button valider = new Button("Valider la tâche");
        valider.getStyleClass().addAll("bouton", "bouton-principal");
        valider.setOnAction(e -> {
            if (courante != null) {
                courante.valider();
                rafraichir();
            }
        });

        Button supprimer = new Button("Supprimer");
        supprimer.getStyleClass().add("bouton");
        supprimer.setOnAction(e -> {
            if (courante != null) {
                gestionnaire.supprimer(courante);
                courante = null;
                rafraichir();
            }
        });
        HBox actions = new HBox(8, valider, supprimer);

        boiteDetail.getChildren().addAll(sousTitreDetail, detailTitre, detailDate,
                boiteEtapes, ligneEtape, actions);
        boiteDetail.setVisible(false);
        boiteDetail.setManaged(false);

        // --- Formulaire d'ajout ---
        Label sousTitreForm = new Label("Nouvelle tâche");
        sousTitreForm.getStyleClass().add("sous-titre");

        champTitre.setPromptText("Titre");
        champDate.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(champDate, Priority.ALWAYS);

        Button creer = new Button("Créer");
        creer.getStyleClass().addAll("bouton", "bouton-principal");
        creer.setOnAction(e -> creerTache());
        HBox ligneDate = new HBox(8, champDate, creer);

        messageErreur.getStyleClass().add("erreur");

        VBox droite = new VBox(12, boiteDetail, new Separator(),
                sousTitreForm, champTitre, ligneDate, messageErreur);
        droite.setPadding(new Insets(14));
        droite.setPrefWidth(320);
        droite.getStyleClass().add("panneau-droit");
        return droite;
    }

    // ---------- Actions ----------

    private void creerTache() {
        String titre = champTitre.getText().trim();
        LocalDate date = champDate.getValue();

        if (titre.isEmpty()) {
            messageErreur.setText("Entrez un titre.");
            return;
        }
        if (date == null) {
            messageErreur.setText("Choisissez une date.");
            return;
        }

        Tache t = new Tache(titre, date);
        gestionnaire.ajouter(t);

        champTitre.clear();
        champDate.setValue(LocalDate.now());
        messageErreur.setText("");

        courante = t;   // sera sélectionnée si elle est visible avec le filtre actuel
        rafraichir();
    }

    private void ajouterEtape() {
        String texte = champEtape.getText().trim();
        if (courante == null || texte.isEmpty()) {
            return;
        }
        courante.ajouterEtape(texte);
        champEtape.clear();
        rafraichir();
    }

    // ---------- Mise à jour de l'affichage ----------

    private void afficherDetail(Tache t) {
        courante = t;
        boolean present = (t != null);
        boiteDetail.setVisible(present);
        boiteDetail.setManaged(present);
        if (!present) {
            return;
        }

        detailTitre.setText(t.getTitre());
        detailDate.setText("Pour le " + t.getDate().format(FORMAT_LONG));

        boiteEtapes.getChildren().clear();
        for (int i = 0; i < t.getEtapes().size(); i++) {
            Etape etape = t.getEtapes().get(i);
            CheckBox c = new CheckBox(etape.getTitre());
            c.setSelected(etape.isFaite());
            int index = i;
            c.setOnAction(ev -> {
                t.setEtapeFaite(index, c.isSelected());
                rafraichir();
            });
            boiteEtapes.getChildren().add(c);
        }
    }

    private void rafraichir() {
        Tache selection = courante;

        affichees.setAll(gestionnaire.filtrer(filtreCourant));
        liste.getSelectionModel().clearSelection();
        if (selection != null && affichees.contains(selection)) {
            liste.getSelectionModel().select(selection);
        }
        liste.refresh();

        // Statistiques
        int somme = 0;
        for (Tache t : gestionnaire.getTaches()) {
            somme += t.getProgression();
        }
        int nb = gestionnaire.getTaches().size();
        int moyenne = (nb == 0) ? 0 : somme / nb;

        statEnCours.setText(String.valueOf(gestionnaire.filtrer(Filtre.EN_COURS).size()));
        statTerminees.setText(String.valueOf(gestionnaire.filtrer(Filtre.TERMINEES).size()));
        statMoyenne.setText(moyenne + "%");

        afficherDetail(liste.getSelectionModel().getSelectedItem());
    }

    // Quelques tâches pour que l'écran ne soit pas vide au lancement
    private void donneesExemple() {
        Tache expose = new Tache("Préparer l’exposé", LocalDate.now().plusDays(3));
        expose.ajouterEtape("Faire les recherches");
        expose.ajouterEtape("Écrire le plan");
        expose.ajouterEtape("Créer les slides");
        expose.setEtapeFaite(0, true);
        expose.setEtapeFaite(1, true);

        Tache td = new Tache("Rendre le TD Java", LocalDate.now().plusDays(5));
        td.ajouterEtape("Lire l’énoncé");
        td.ajouterEtape("Coder le modèle");
        td.ajouterEtape("Coder l’interface");
        td.ajouterEtape("Relire et tester");
        td.setEtapeFaite(0, true);

        Tache partiel = new Tache("Réviser le partiel", LocalDate.now().plusDays(12));

        Tache cv = new Tache("Envoyer le CV", LocalDate.now().minusDays(1));
        cv.valider();

        gestionnaire.ajouter(td);
        gestionnaire.ajouter(partiel);
        gestionnaire.ajouter(cv);
        gestionnaire.ajouter(expose);
        gestionnaire.trierParDate();
    }

    public static void main(String[] args) {
        launch(args);
    }
}