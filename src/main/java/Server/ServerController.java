package Server;

import CommonUtils.Utente;
import CommonUtils.Utenti;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import static java.lang.System.out;

/**
 * Questa classe verrà utilizzata per controllare i bottoni del server
 */
public class ServerController {
    @FXML
    public TextArea textAreaLBUtente;
    @FXML
    public TextArea textAreaLBServer;
    @FXML
    ListView<Utente> ServerListView;
    ObservableList<Utente> users;
    static Utenti utenti;
    ListProperty<Utente> usersProperty;
    private Server server;

    public void initialize() {
        System.out.println("riempio gli utenti");
        server = null;
        utenti = new Utenti();
        utenti.fill();
        users = FXCollections.observableArrayList(utenti.getUtenti());
        usersProperty = new SimpleListProperty<>(users);
        ServerListView.itemsProperty().bindBidirectional(usersProperty);
        textAreaLBServer.setWrapText(true);
        textAreaLBUtente.setWrapText(true);
        //È un binding con la property della classe Utente che conserva i messaggi del Utente stesso
        ServerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                textAreaLBUtente.textProperty().bind(newValue.getMsgToServer());
            } else {
                textAreaLBUtente.clear();
            }
        });
        textAreaLBServer.appendText("Server online, in attesa di connesioni...");
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }


    /**
     * viene utilizzato per aggiungere informazione al logbook dell'interfaccia grafica del server
     */
    public void updateLogbookServer(String s) {
        textAreaLBServer.appendText('\n' + s);
    }

    public Utenti getUtenti() {
        return utenti;
    }
}