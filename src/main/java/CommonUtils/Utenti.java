package CommonUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Utenti {
    private static final String filePath = "src/main/resources/Server/listaUtenti.xml";
    private static final String userEmailsPath = "src/main/resources/userEmails/";
    private final List<Utente> utenti;
    private final EmailXMLWriterReader emailRW;

    public Utenti() {
        this.utenti = new ArrayList<>();
        emailRW = new EmailXMLWriterReader();
    }

    public List<Utente> getUtenti() {
        return utenti;
    }

    public Utente getUtente(String usename) {
        for (Utente u : getUtenti()) {
            if (Objects.equals(u.getUsername(), usename))
                return u;
        }
        return null;
    }

    public List<String> getUsernames() {
        ArrayList<String> res = new ArrayList<>();
        for (Utente u : utenti) {
            res.add(u.getUsername());
        }
        return res;
    }

    /**
     * funzione che se viene aggiunto un utente dalla Login lo aggiunge al vettore utenti
     * e lo aggiunge anche nella listaUtenti.xml
     * Se invece l'utente viene rimosso allora in quel caso lo si rimuove dal vettore Utenti
     * e anche dalla ListaUtenti.
     * inoltre per ogni nuovo utente va anche a creare un file username.xml che conterrà le sue mail.
     * quando l'utente viene rimosso anche il file viene eliminato
     */
    public void updateUtenti(Utente utente, String op) {
        if (op.equals("add")) {
            utenti.add(utente);
            newDoc(userEmailsPath + utente.getUsername() + ".xml");
        } else if (op.equals("rm")) {
            utenti.remove(utente);
            rmDoc(userEmailsPath + utente.getUsername() + ".xml");
        } else {
            System.err.println("operazione su updateUtenti non corretta");
        }
        try {
            DocumentBuilder builder;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element rootElement = document.createElement("utenti");
            document.appendChild(rootElement);
            for (Utente u : utenti) {
                Element utenteElement = document.createElement("utente");
                utenteElement.setTextContent(u.getUsername());
                rootElement.appendChild(utenteElement);
            }
            emailRW.transformer(document, filePath);

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private void newDoc(String filePath) {
        emailRW.newFile(filePath);
       // emailRW.addEmail(new Email(), filePath);
//        emailRW.updateXML(emails, filePath);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void rmDoc(String filePath) {
        File file = new File(filePath);
        if (file.exists())
            file.delete();
    }

    /**
     * questo metodo viene chiamato per inizializzare gli Utenti, vengono salvati
     * in un file XML e aggiunti in un vettore
     */
    public void fill() {
        try {
            // Carica il file XML
            File file = new File(filePath);
            NodeList userList = exstractList(file);
            formNodeListToArrayList(userList, (ArrayList<Utente>) utenti);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * questa funzione prende il nodelist estratto dal XML, e lo mette dentro un arrayList
     */
    public ArrayList<Utente> formNodeListToArrayList(NodeList nodeList, ArrayList<Utente> utenti) {
        ArrayList<Utente> users = utenti;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node userNode = nodeList.item(i);
            if (userNode.getNodeType() == Node.ELEMENT_NODE) {
                Element userElement = (Element) userNode;
                String username = userElement.getTextContent();
                users.add(new Utente(username));
            }
        }
        return users;
    }

    /**
     * funzione che estrae una NodeList dalla parsificazione di listaUtenti.xml
     */
    public static NodeList exstractList(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(file);

        // Ottieni l'elemento radice "utenti"
        Element rootElement = doc.getDocumentElement();

        // Ottieni la lista degli elementi figlio "utente"
        return rootElement.getElementsByTagName("utente");
    }

}
