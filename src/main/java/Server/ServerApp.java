package Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerApp extends Application {
    @Override
    public void start(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApp.class.getResource("ServerView.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            System.err.println("error in start method of ServerApp");
            e.printStackTrace();

        }
        ServerController controller = fxmlLoader.getController();
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();


        //viene creato un thread separato per non interferire
        //  con quello che costruisce l'intefaccia utente
        Thread serverThread = new Thread(() -> {
            Server s = new Server(controller);
            controller.setServer(s);
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    /**
     * il metodo per lanciare il main Server
     */
    public static void main(String[] args) {
        launch();
    }
}