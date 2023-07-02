package Client;

import CommonUtils.Email;
import CommonUtils.Utente;
import CommonUtils.OperationObj;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ClientController {
    @FXML
    public Text destinatariTextField;
    @FXML
    public Text mittenteTextField;
    @FXML
    public Text oggettoTextField;
    @FXML
    public TextArea emailTextArea;
    @FXML
    public ListView<Email> mailListView;


    private static String username;

    private Client client;
    private static ArrayList<Email> emailsArray = new ArrayList<>();
    public static ObservableList<Email> emails;
    public ListProperty<Email> emailListProperty;
    private static ScheduledExecutorService threadpool;


    public void initialize(Utente utente) {

        if (utente != null) {
            username = utente.getUsername();
            System.out.println("Benevenuto: " + username);
            emailTextArea.setEditable(false);
            client = new Client(username);
            //BIND  EMAILS
            emails = FXCollections.observableArrayList(emailsArray);
            emailListProperty = new SimpleListProperty<>(emails);
            mailListView.itemsProperty().bindBidirectional(emailListProperty);
            mailListView.setOnMouseClicked(this::showSelectedEmail);


            //THREADPOOL SCHEDULED
            threadpool = Executors.newScheduledThreadPool(1);
            threadpool.scheduleAtFixedRate(() -> {
                try {
                    ArrayList<Email> temp = client.checkNews();
                    if (temp != null) {
                        Platform.runLater(() -> {
                            emailsArray = temp;
                            addInProperty();
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("erroe in theadScheduling");
                }
            }, 5, 15, TimeUnit.SECONDS);

        }
    }

    /**
     * metodo che mostra la mail selezionata nei campi corretti
     */
    private void showSelectedEmail(javafx.scene.input.MouseEvent mouseEvent) {
        Email email = mailListView.getSelectionModel().getSelectedItem();
        if (email != null) {
            mittenteTextField.setText(email.getSender());
            destinatariTextField.setText(email.getRecipients());
            oggettoTextField.setText(email.getObject());
            emailTextArea.setWrapText(true);
            emailTextArea.setText(email.getDate() + '\n' +
                    email.getText());
        }
    }

    /**
     * gestisce il bottone "Scrivi", prima crea la mail da inviare
     * e successivamente la manda tramite ObjectOutputStream
     */
    public void onWriteButton() {
        Email e = createDialog(username, "", "", "");
        if (e != null) {
            boolean sended = client.sendNewEmail(e, new OperationObj("sendEmail", username));
            showTooltip("email inviata");
            if (!sended)
                showAlert("Server offline");
        } else System.err.println("L'invio della mail non è stato possibile");
    }


    public void onReloadBtn() {
        System.out.println("proviamo ad Aggiornare la lista di email");
        emailsArray = client.reload(new OperationObj("emails", username));
        if (emailsArray == null) {
            showAlert("Sever offline");
            return;
        }
        addInProperty();
    }

    public void onDeleteBtn() {
        Email emailToDelete = mailListView.getSelectionModel().getSelectedItem();
        emailsArray.remove(emailToDelete);
        mailListView.getSelectionModel().clearSelection();
        setVoidEmail();
        client.deleteEmail(emailToDelete.getId(), new OperationObj("emailEliminata", username));
        emailListProperty.remove(emailToDelete);
    }

    /**
     * metodo che svuota tutti i campi della mail, se nessana email è stata selezionata
     */
    private void setVoidEmail() {
        mittenteTextField.setText("");
        destinatariTextField.setText("");
        oggettoTextField.setText("");
        emailTextArea.setText("");
    }

    /**
     * controller per il bottone 'risposta'
     */
    public void onAnswerBtn() {
        Email email = mailListView.getSelectionModel().getSelectedItem();
        String subj = "RE: " + email.getObject();
        String recips = email.getSender();
        Email answer = createDialog(username, subj, recips, "");
        if (errorEmail(answer))
            return;
        client.sendNewEmail(answer, new OperationObj("sendEmail", username));
        showTooltip("email inviata");
    }

    /**
     * controller per il bottone 'inoltra'
     */
    public void onForwardBtn() {
        Email email = mailListView.getSelectionModel().getSelectedItem();
        String subj = "FW: " + email.getObject();
        String text = "INOLTRATA" + email.getText() + "\n ----------------";
        Email answer = createDialog(username, subj, "", text);
        if (errorEmail(answer))
            return;
        client.sendNewEmail(answer, new OperationObj("sendEmail", username));
        showTooltip("email inviata ");


    }

    /**
     * controller per il bottone 'rispondi a tutti'
     */

    public void onReplyALLBtn() {
        Email email = mailListView.getSelectionModel().getSelectedItem();
        String subj = "RE: " + email.getObject();
        String[] oldRecips = email.getRecipients().split(",");
        String recips = email.getSender();
        for (String s : oldRecips) {
            s = s.trim();
            if (!s.equals(username))
                recips = recips + ',' + s;
        }
        Email answer = createDialog(username, subj, recips, "");
        if (errorEmail(answer))
            return;
        client.sendNewEmail(answer, new OperationObj("sendEmail", username));
    }

    /**
     * Crea una finestra di dialogo che consente di comporre una mail, restituisce l'oggeto mail creato
     */
    public Email createDialog(String username, String subj, String recips, String text) {
        AtomicReference<Email> ress = new AtomicReference<>(); //evita problemi di sincronizzazione
        Dialog<Email> dialog = new Dialog<>();
        dialog.getDialogPane().setPrefHeight(400);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.setTitle("Scrivi una mail");
        dialog.setHeaderText("Inserisci i dettagli");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        ColumnConstraints colonna1 = new ColumnConstraints(75);
        gridPane.getColumnConstraints().add(colonna1);
        TextField mittente = new TextField(username);
        mittente.setEditable(false);
        TextField oggetto = new TextField();
        oggetto.setText(subj);
        TextField destinatari = new TextField();
        destinatari.setText(recips);
        destinatari.setPromptText("elemento obbligatorio, separare con ','");
        TextArea emailTextField = new TextArea();
        emailTextField.setText(text);

        gridPane.add(new Label("Mittente:"), 0, 0);
        gridPane.add(mittente, 1, 0);
        gridPane.add(new Label("Destinatari"), 0, 1);
        gridPane.add(destinatari, 1, 1);
        gridPane.add(new Label("Oggetto"), 0, 2);
        gridPane.add(oggetto, 1, 2);
        gridPane.add(new Label("Testo:"), 0, 3);
        gridPane.add(emailTextField, 1, 3);

        dialog.getDialogPane().setContent(gridPane);

        // Aggiunge pulsanti di conferma e annulla
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String name = mittente.getText();
                String dest = destinatari.getText();
                String obj = oggetto.getText();
                String email = emailTextField.getText();
                if (!checkRecipients(dest)) return null;
                //return createDialog(name, obj, "", email);
                // Puoi eseguire le operazioni desiderate con i valori inseriti
                return new Email(name, dest, obj, email);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            // Esegui le operazioni desiderate con il risultato
            System.out.println("Risultato: mail creata con successo ");

            Email e = new Email(result.getSender(), result.getRecipients(), result.getObject(), result.getText());
            ress.set(e);
        });
        return ress.get();
    }

    /**
     * il metodo controlla che che il campo recipients sia non vuoto e che gli elementi inseriti facciano parte
     * di Utenti, più utenti vanno separati da una virgola
     */
    private boolean checkRecipients(String destinatari) {
        if (destinatari.isEmpty()) {
            // Mostra un messaggio di errore o gestisci l'errore nel modo desiderato
            showAlert("il campo 'Destinatari' non può essere vuoto");
            return false;
        }
        String[] recips = destinatari.split(",");
        for (String s : recips) {
            s = s.trim(); //rimuove gli spazi bianchi
            if (!isFormatted(s)) {
                showAlert("formato email non corretto (destinatari) ");
                return false;
            }

        }
        return true;
    }

    /**
     * il metodo controlla il formato delle Stringhe, devono essere email
     * @param s: la stringa da controllare
     * @return true if s is emailFormatted, else false
     */

    private boolean isFormatted(String s) {
        if (s.contains("@")) {
            String[] parts = s.split("@");
            boolean first = parts[0].matches("^[a-zA-Z0-9]*\\.?[a-zA-Z0-9]*$");
            boolean second = parts[1].equals("gmail.com");
            return first && second;
        }
        return false;
    }

    /**
     * il metodo va a spegnere il threadpool, così da far terminare tutti i processi alla chiusura
     * dell'applicazione javaFX
     */

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void shutdown() {
        if (threadpool.isShutdown())
            return;
        threadpool.shutdown();
        try {
            threadpool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Threapool client non interrotto correttamente");
        }
        Platform.exit();
    }

    /**
     * Metodo che fa comparire una finestra di Alert
     *
     * @param msg : si tratta del messaggio che verrà mostrato dall'alert
     */
    private static void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private static boolean errorEmail(Email answer) {
        if (answer == null) {
            System.err.println("nessuna email inviata");
            return true;
        }
        return false;
    }

    /**
     * msotra un tooltip, per nuove email in arrivo e per quando viene inviata una mail
     * @param msg: testo mostrato
     */
    private void showTooltip(String msg) {
        Tooltip tooltip = new Tooltip(msg);
        Window w = mailListView.getScene().getWindow();
        tooltip.setFont(new Font(15));
        tooltip.setStyle("-fx-text-fill: green");
        tooltip.show(w, w.getX() + w.getWidth() / 2 - 39, w.getY() + w.getHeight() - 40);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> tooltip.hide());
        pause.play();
    }

    /**
     * Il metodo va ad aggiornare la variabile ArrayList<Email> con una nuova lista
     */
    public static void updateEmailsArray(ArrayList<Email> e) {
        emailsArray = e;
    }

    /**
     * metodo che aggiunge tutte le email non presenti nella property, per sincronizzare la listView
     */
    private void addInProperty() {
        for (Email e : emailsArray)
            if (!emailListProperty.contains(e)) {
                emailListProperty.add(e);
                showTooltip("nuova email");
            }

    }
}
