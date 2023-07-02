package Client;

import CommonUtils.Utente;
import CommonUtils.Utenti;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML
    public Button newUserBUtton;
    @FXML
    public ListView<Utente> UtentilistView;
    @FXML
    public Button deleteUserButton;

    public static Utenti utenti;
    public ObservableList<Utente> users;
    public ListProperty<Utente> usersProperty;

    public void initialize() {
        utenti = new Utenti();
        System.out.println("riempio gli utenti");

        utenti.fill();
        users = FXCollections.observableArrayList(utenti.getUtenti());
        usersProperty = new SimpleListProperty<>(users);
        UtentilistView.itemsProperty().bindBidirectional(usersProperty);
        UtentilistView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Doppio clic
                Utente selectedItem = UtentilistView.getSelectionModel().getSelectedItem();
                Stage stage = ((Stage) ((Node) event.getSource()).getScene().getWindow());
                stage.close();
                try {
                    openSelectedUser(selectedItem);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    /**
     * handler per aprire lo User selezionato, necessita di due click
     */
    private void openSelectedUser(Utente utente) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("ClientView.fxml"));
        Parent root = fxmlLoader.load();
        ClientController c = fxmlLoader.getController();
        c.initialize(utente);
        Stage stage = new Stage();
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle(utente.getUsername());
        stage.setScene(scene);
        stage.setOnCloseRequest(windowEvent -> ClientController.shutdown());
        stage.show();
    }

    /**
     * handler che gestisce il bottone "nuovoUtente", apre una finestra di dialogo in cui inserire il nome utente
     * successivamente cattura lo username e lo usa per creare un nuovo utente e lo aggiunge alla lista "utenti"
     * Aggiorna la listView e con il comando "updateUtenti" viene aggiornato anche il corrispondente file
     * XML listaUtenti.xml
     */

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void createNewUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Creazione Utente");
        dialog.setHeaderText("Inserisci nome Utente");
        dialog.setContentText("nome:");


        // Mostra il dialogo e aspetta la risposta dell'utente
        Optional<String> result = dialog.showAndWait();
        if (result.get().equals("")) {
            System.err.println("non si può creare un utente senza un nome");
            return;
        }
        if (utenti.getUsernames().contains(result.get())) {
            System.err.println("Utente non valido");
            return;
        }
        Utente utente = new Utente(result.get());
        utenti.updateUtenti(utente, "add");
        users.add(utente);
    }

    /**
     * deleteUser rimuove l'elemento selezionato dalla ListView e dall'array
     * inoltre la funzione tooltip ci spiega che dobbiamo selezionare una mail
     */

    public void deleteUser() {
        Utente utente = UtentilistView.getSelectionModel().getSelectedItem();
        users.remove(utente);
        utenti.updateUtenti(utente, "rm");
    }

    /**
     * fa apparire un consiglio per il bottone elimina
     */
    public void infoDeleteButton() {
        Tooltip tooltip = new Tooltip("Seleziona prima un account per eliminarlo");
        Bounds bounds = deleteUserButton.localToScreen(deleteUserButton.getBoundsInLocal());
        double buttonX = bounds.getMinX();
        double buttonY = bounds.getMinY();

        // Calcola la posizione del tooltip sopra il bottone
        double tooltipX = buttonX - 8;// + deleteUserButton.getWidth() / 2 - tooltip.getWidth() / 2;
        double tooltipY = buttonY - tooltip.getHeight() - 10; // Spostamento verticale sopra il bottone

        tooltip.show(deleteUserButton, tooltipX, tooltipY);
        deleteUserButton.setOnMouseExited(mouseExitded -> tooltip.hide());
    }

}
