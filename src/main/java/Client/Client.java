package Client;

import CommonUtils.Email;
import CommonUtils.OperationObj;
import Server.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * la classe si occuperà della connessione con il Server
 */
public class Client {
    private final int serverPORT = Server.getPORT();
    private final int timer = 1000;// 10 secondi
    Socket socket;
    private ObjectOutputStream objOutStream;
    private ObjectInputStream objInStream;
    private final String username;

    /**
     * Quando un client viene creato, tenta subito di mettersi in contatto con il Server
     * e lancia il comando "emails" per ricevere la sua lista di emails
     * NON C'È PERSISTENZA SUL CLIENT
     */
    public Client(String username) {
        this.username = username;
        socket = connToServer(serverPORT, timer);
        if (socket != null) {

            try {
                //invio richiesta "emails" sempre la prima richiesta al Server
                objOutStream = new ObjectOutputStream(socket.getOutputStream());
                sendRequest(new OperationObj("emails", username));

                //RICEZIONE RISPOSTA
                ArrayList<Email> emails = receive();
                ClientController.updateEmailsArray(emails); //impostiamo l'arralist da cui verrà fatto il binding ?
                System.out.println("file ricevuto");

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("error in Client constructor");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                closeConnections();
            }
        }

    }

    /**
     * metodo che tenta di connettersi al server, ci prova per 10 s, se si connette restituisce un socket
     */
    public Socket connToServer(int serverPORT, int timer) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timer;
        while (System.currentTimeMillis() < endTime) {
            try {
                socket = new Socket("localhost", serverPORT);
                System.out.println("Connesione al server, " + username);
                return socket;

            } catch (IOException e) {
                System.out.println("Connessione fallita... RIPROVO");
            }
        }
        System.out.println("Tempo per la connessione scaduta");
        return null;
    }

    /**
     * metodo delegato a inviare un richiesta di operazione al Server
     */
    public void sendRequest(OperationObj op) throws IOException {
        try {
            objOutStream.writeObject(op);
            objOutStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error on method sendRequest in class Client");
        }
    }

    /**
     * funzione delegata a ricevere un arrayList di mail aggiornate dal server
     *
     * @return ArrayList<Email> nuove email
     */
    public ArrayList<Email> receive() throws IOException {
        ArrayList<Email> emails = new ArrayList<>();
        try {
            objInStream = new ObjectInputStream(socket.getInputStream());
            Object temp = objInStream.readObject();
            emails = (ArrayList<Email>) temp;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error in receive method in  class Client, IOException");
        } catch (ClassNotFoundException e) {
            System.err.println("error in receive method in class Client, ClassNotFoundException");
            e.printStackTrace();
        }
        return emails;
    }

    public boolean sendNewEmail(Email e, OperationObj op) {
        try {
            socket = connToServer(serverPORT, timer);
            if (socket == null)
                return false;
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            sendRequest(op);
            objOutStream.writeObject(e);
            objOutStream.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("error in method sendNewEmail");
        } finally {
            closeConnections();
        }
        return true;
    }

    public ArrayList<Email> reload(OperationObj op) {
        try {
            socket = connToServer(serverPORT, timer);
            if (socket == null)
                return null;
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            sendRequest(op);
            return receive();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error in method reload in class Client");
        } finally {
            closeConnections();
        }
        return null;
    }

    /**
     * il metodo stabilisce una connessione con il Server,
     * quindi invia l'id da eliminare
     *
     * @param id: identificatore della email da eliminare dal file XML
     * @param op: parametro che porta due informazioni: l'operazione che il server deve
     *            svolgere e il richiedente dell'operazione
     */
    public void deleteEmail(int id, OperationObj op) {
        socket = connToServer(serverPORT, timer);
        if (socket == null)
            return;
        try {
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            sendRequest(op);
            objOutStream.writeObject(id);
            objOutStream.flush();
        } catch (IOException e) {
            System.err.println("error in method deleteEmail of Client class");
            e.printStackTrace();
        } finally {
            closeConnections();
        }
    }

    public ArrayList<Email> checkNews() throws IOException {
        try {
            socket = connToServer(serverPORT, timer);
            if (socket == null)
                return null;
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            sendRequest(new OperationObj("news", username));
            objInStream = new ObjectInputStream(socket.getInputStream());
            String response = (String) objInStream.readObject();
            if (response.equals("si")) {
                System.out.println("la risposta é si");
                return receive();
            } else {
                System.out.println("la risposta è 'NO'");
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("error in method checkNews in class Client");
        } finally {
            closeConnections();
        }
        return null;
    }

    private void closeConnections() {
        try {
            if (objOutStream != null)
                objOutStream.close();
            if (objInStream != null)
                objInStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("connesione chiusa");
    }
}
