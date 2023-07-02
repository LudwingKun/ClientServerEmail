package Server;

import CommonUtils.EmailXMLWriterReader;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8289;
    private static final int THREADPOOL_SIZE = 10;
    private ExecutorService executorService;
    private ServerSocket serverSocket;
    private static boolean stop = false;

    /**
     * Questa classe gestisce le interazioni con il Client, fa uso di un
     * thradpool di 10 thread, il Server rimane in atttesa di connessioni
     * una volta ricevuta la connessione viene creato un oggeto Runnable che
     * gestirà la richiesta
     */

    public Server(ServerController controller) {
        Stage stage = (Stage) controller.textAreaLBUtente.getScene().getWindow();
        stage.setOnCloseRequest(windowEvent -> setStop(true));
        try {
            serverSocket = new ServerSocket(PORT);
            EmailXMLWriterReader emailXMLWriterReader = new EmailXMLWriterReader();
            executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE);
            System.out.println("Server online,  in ascolto");
            while (!stop) {
                Socket clientSocket = serverSocket.accept();
                controller.updateLogbookServer("Client connesso: gestione richiesta ...");
                executorService.execute(new ClientHandler(clientSocket, controller, emailXMLWriterReader ));

            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("errore nel costruttore Server");
        } finally {
            controller.updateLogbookServer("Server down");
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            executorService.shutdown();
        }
    }

    /**
     * spegne il Server
     *
     * @param b: se true il server viene  spento
     */
    public void setStop(boolean b) {
        stop = b;
        assert !executorService.isShutdown();
        executorService.shutdown();
    }

    public static int getPORT() {
        return PORT;
    }
}
