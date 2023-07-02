package Server;

import CommonUtils.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private OperationObj op;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private final ServerController controller;
    private final Utenti users;
    private EmailXMLWriterReader emailRW;

    public ClientHandler(Socket clientSocket, ServerController controller, EmailXMLWriterReader emailRW) {
        this.clientSocket = clientSocket;
        this.controller = controller;
        users = controller.getUtenti();
        this.emailRW = emailRW;
    }

    @Override
    public void run() {
        try {
            op = acceptRequest();
            assert op != null;
            if (!op.getOp().equals("news")) {
                controller.updateLogbookServer("nuova request da: " + op.getAsker());
                updateMSGClient("Client connesso, richiesta: " + op.getOp(), op.getAsker());
            }
            String listMailPath = "src/main/resources/userEmails/";
            switch (op.getOp()) {
                case "emails" -> {
                    try {
                        String savePath = listMailPath + op.getAsker() + ".xml";
                        send(savePath);
                        clientSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("error in run method of ClientHandler, emails case");
                    }
                }
                case "sendEmail" -> {
                    Email e = (Email) objectInputStream.readObject();
                    String[] recipients = e.getRecipients().split(",");
                    //le Email vengono salvate in tutti i file di destinazione
                    for (String s : recipients) {
                        s = s.trim();
                        if (users.getUsernames().contains(s)){
                            String recipsPath = listMailPath + s + ".xml";
                            Utente u = users.getUtente(s);
                            u.setNews(true);
                            emailRW.addEmail(e, recipsPath);
                        }else updateMSGClient("ERROR: la mail: " + s +" non esiste", op.getAsker());
                    }
                    updateMSGClient("ricezione server, smistamento...", op.getAsker());
                    clientSocket.close();
                }
                case "emailEliminata" -> {
                    int idEmailToDelete = (int) objectInputStream.readObject();
                    emailRW.rmEmail(idEmailToDelete, listMailPath + op.getAsker() + ".xml");
                    updateMSGClient("email non più persistente", op.getAsker());
                    clientSocket.close();
                }
                case "news" -> {
                    objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    boolean news = users.getUtente(op.getAsker()).getNews();
                    if (news) {
                        updateMSGClient("CI SONO NOVITÀ, aggiornamento email...", op.getAsker());
                        objectOutputStream.writeObject("si");
                        objectOutputStream.flush();
                        send(listMailPath + op.getAsker() + ".xml");
                        users.getUtente(op.getAsker()).setNews(false);
                    } else {
                        updateMSGClient("NIENTE NOVITÀ", op.getAsker());
                        objectOutputStream.writeObject("no");
                        objectOutputStream.flush();
                    }
                }
            }
            updateMSGClient("richiesta evasa", op.getAsker());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("errore metodo run ClientHandler");
        } catch (ClassNotFoundException e) {
            System.err.println("error in second catch of run on clientHandler");
            throw new RuntimeException(e);
        } finally {
            closeConnections();
            controller.updateLogbookServer("Connessione chiusa con:" + op.getAsker());
            System.out.println("connesione chiusa con: " + op.getAsker());
        }

    }

    public void send(String path) {
        try {
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            File fileToSend = new File(path);
            ArrayList<Email> emails = emailRW.readEmailFromXMl(fileToSend.getPath());
            objectOutputStream.writeObject(emails);
            objectOutputStream.flush();
            updateMSGClient("Emails inviate", op.getAsker());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error in send method of the class ClientHandler");
        }
    }

    /**
     * il metodo scarica tutte le email nuove
     * @param path: indica da quale file prendere le email da aggiungere all'arrayList
     */
    public void sendOnlyNews(String path){
        try {
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ArrayList<Email> newEmails = emailRW.readFullPower(path,"all");
            emailRW.makeRead(path);
            objectOutputStream.writeObject(newEmails);
            objectOutputStream.flush();
            updateMSGClient("Email inviate...", op.getAsker());
            System.out.println("utilizzo sendOnlyNews, la lunghezza del vettoe è: " + newEmails.size());
        } catch (IOException e) {
            System.err.println("error in sendOnlyNews method of class ClientHandler");
            e.printStackTrace();
        }
    }

    /**
     * Il metodo restituisce l'operazione richiesta dal client
     */
    private OperationObj acceptRequest() {
        try {
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            return (OperationObj) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("error in acceptRequest of ClientHandler");
            e.printStackTrace();
        }
        return null;
    }

    private void updateMSGClient(String msg, String username) {
        Utente u = users.getUtente(username);
        u.concatProperty(msg);
    }

    private void closeConnections() {

        try {
            if (objectOutputStream != null)
                objectOutputStream.close();
            if (objectInputStream != null)
                objectInputStream.close();
            if (clientSocket != null)
                clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error in close connections of ClientHandler");
        }

    }
}
