package CommonUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Utente {
    private final String username;
    private boolean news;
    private final StringProperty msgToServer;

    public Utente(String username) {
        this.username = username;
        msgToServer = new SimpleStringProperty(username);
        news = false;
    }

    public boolean getNews() {
        return news;
    }

    public StringProperty getMsgToServer() {
        return msgToServer;
    }
    public void concatProperty(String s){
        String temp = msgToServer.get() + "\n" +s;
        this.msgToServer.set(temp);
    }

    @Override
    public String toString() {
        return username;
    }


    public void setNews(boolean news) {
        this.news = news;
    }

    public String getUsername() {
        return username;
    }


}
