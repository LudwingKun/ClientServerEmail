package CommonUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * classe che manipola oggetti List<Email> e file XML
 */
public class EmailXMLWriterReader {

    private final ReadWriteLock lock;

    public EmailXMLWriterReader() {
        lock = new ReentrantReadWriteLock();
    }



    public void makeRead(String filepath) {
        lock.readLock().lock();
        Document doc = createDocument(new File(filepath));
        lock.readLock().unlock();
        System.out.println("cambio lo stato news di tutte le email inviate...");
        lock.writeLock().lock();
        NodeList list = doc.getElementsByTagName("news");
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getTextContent().equals("true"))
                list.item(i).setTextContent("false");
        }
        transformer(doc, filepath);
        lock.writeLock().unlock();
    }


    /**
     * il metodo rimuove la mail dall'arrayList, utilizzando l'id della mail stessa
     * il quale viene assegnato uguale all'indice dell'arraylist
     *
     * @param id:       Rappresenta la l'id della email da eliminare
     * @param filePath: è il percorso del file da cui eliminare la email
     */
    public void rmEmail(int id, String filePath) {
        try {
            // Carica il file XML
            lock.readLock().lock();
            Document doc = createDocument(new File(filePath));
            lock.readLock().unlock();

            System.out.println("doc creation ...");
            lock.writeLock().lock();
            NodeList list = doc.getElementsByTagName("email");
            if (list != null && list.item(id) != null) {
                list.item(id).getParentNode().removeChild(list.item(id));
                NodeList listaId = doc.getElementsByTagName("id");
                for (int i = 0; i < list.getLength(); i++) {
                    int idEl = Integer.parseInt(listaId.item(i).getTextContent());
                    if (idEl > id)
                        listaId.item(i).setTextContent(String.valueOf(idEl - 1));
                }
            }
            transformer(doc, filePath);
        } catch (Exception e) {
            System.err.println("error thro by rmEmail of EmailXMLWriterReader...");
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * funzione che modifica il file di salvataggio, aggiungendo una email passata come parametro
     *
     * @param email:    la email da aggiungere
     * @param filePath: il percorso in cui salvare la mail, è asssociata all'utente
     */
    public void addEmail(Email email, String filePath) {
        try {
            lock.writeLock().lock();
            email.setNews(true);
            File file = new File(filePath);
            Document doc = createDocument(file);
            System.out.println("doc create...");
            NodeList list = doc.getElementsByTagName("email");
            email.setId(list.getLength());
            if (list.getLength() > 0) {
                list.item(list.getLength() - 1).getParentNode().appendChild(createEmailElement(doc, email));
                transformer(doc, filePath);
            } else {
                System.out.println("lista mail vuota...");
                Document document = createDocument(null);
                Element rootElement = document.createElement("emails");
                document.appendChild(rootElement);
                rootElement.appendChild(createEmailElement(document, email));
                transformer(document, filePath);
            }
        } finally {
            lock.writeLock().unlock();
        }

    }

    /**
     * crea un nuovo file di salvataggio email quando viene creato un nuovo Utente
     * @param filepath: percorso in cui sarà creato questo nuovo File
     */
    public void newFile(String filepath) {
        lock.writeLock().lock();
        Document document = createDocument(null);
        Element rootElement = document.createElement("emails");
        document.appendChild(rootElement);
        transformer(document, filepath);
        lock.writeLock().unlock();
    }

    /**
     * funzione che legge un file XML, che è una lista di email, e le scrive su un ArrayList
     *
     * @return ArrayList<Email>
     */
    public ArrayList<Email> readEmailFromXMl(String filePath) {
        lock.readLock().lock();
        ArrayList<Email> emails = new ArrayList<>();
        try {
            File xmlFile = new File(filePath);
            Document document = createDocument(xmlFile);
            Element rootElement = document.getDocumentElement();
            NodeList emailList = rootElement.getElementsByTagName("email");
            for (int i = 0; i < emailList.getLength(); i++) {
                Element emailElement = (Element) emailList.item(i);
                Email email =extractEmailFromDOC(emailElement,emails.size());
                emails.add(email);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("error in readFromXml");
        } finally {
            lock.readLock().unlock();
        }
        return emails;
    }
//TODO: far funzionare questo!!!!
    public ArrayList<Email> readFullPower(String filePath, String op) {
        lock.readLock().lock();
        ArrayList<Email> emails = new ArrayList<>();
        File file = new File(filePath);
        Document doc = createDocument(file);
        NodeList emailList = doc.getElementsByTagName("email");
        if (op.equals("all")) {
            for (int i = 0; i < emailList.getLength(); i++) {
                Element emailElement = (Element) emailList.item(i);
                Email e = extractEmailFromDOC(emailElement, emails.size());
                //TODO:togliere
                System.out.println("stampa su ALl");
                emails.add(e);
            }return emails;
        } else if (op.equals("news")) {
            NodeList newsList = doc.getElementsByTagName("news");
            for (int i = 0; i < emailList.getLength(); i++) {
                if (newsList.item(i).getTextContent().equals("true")) {
                    Element emailElement = (Element) emailList.item(i);
                    Email e = extractEmailFromDOC(emailElement, emailList.getLength());
                    //TODO: togliere
                    System.out.println("stampa su news");
                    emails.add(e);
                }
            }return emails;
        }
        lock.readLock().unlock();
        return emails;
    }

    private Email extractEmailFromDOC(Element emailElement, int pos) {
        String sender = emailElement.getElementsByTagName("sender").item(0).getTextContent();
        String recipients = emailElement.getElementsByTagName("recipients").item(0).getTextContent();
        String object = emailElement.getElementsByTagName("object").item(0).getTextContent();
        String text = emailElement.getElementsByTagName("text").item(0).getTextContent();
        String date = emailElement.getElementsByTagName("date").item(0).getTextContent();
        return new Email(sender, recipients, object, text, date, pos);
    }
    /**
     * operaioni di base per creare un Document
     *
     * @return l'oggetto Document, non è ancora associato a nessun file
     */
    private Document createDocument(File file) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            if (file == null)
                document = builder.newDocument();
            else
                document = builder.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.err.println("error in createDocument...");
            e.printStackTrace();
        }
        return document;
    }


    /**
     * metodo che fa a trasformare effettivamente la lista di email in un xml
     * IMPORTATISSIMO
     */
    public void transformer(Document document, String filePath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // Salva le modifiche nel file XML
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);

            System.out.println("Modifiche salvate correttamente nel file XML.");
        } catch (Exception e) {
            System.out.println("Errore durante il salvataggio delle modifiche nel file XML:");
            e.printStackTrace();
        }
    }

    /**
     * il metodo crea un Element XML da aggiungere al file di salvataggio
     *
     * @param document: il document a cui aggiungere l'elemento
     * @param email:    rappresenta l'oggetto java d aggiungere
     * @return Element creato basandosi sulla Email
     */
    public Element createEmailElement(Document document, Email email) {
        Element emailElement = document.createElement("email");

        Element senderElement = document.createElement("sender");
        senderElement.setTextContent(email.getSender());
        emailElement.appendChild(senderElement);

        Element recipientElement = document.createElement("recipients");
        recipientElement.setTextContent(email.getRecipients());
        emailElement.appendChild(recipientElement);

        Element subjectElement = document.createElement("object");
        subjectElement.setTextContent(email.getObject());
        emailElement.appendChild(subjectElement);

        Element messageElement = document.createElement("text");
        messageElement.setTextContent(email.getText());
        emailElement.appendChild(messageElement);

        Element dateElement = document.createElement("date");
        dateElement.setTextContent(email.getDate());
        emailElement.appendChild(dateElement);

        Element isReadElement = document.createElement("news");
        isReadElement.appendChild(document.createTextNode(Boolean.toString(email.getNews())));
        emailElement.appendChild(isReadElement);

        Element id = document.createElement("id");
        id.setTextContent(String.valueOf(email.getId()));
        emailElement.appendChild(id);

        return emailElement;
    }
}

