package CommonUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Email implements  Serializable{
    private String sender;
    private String recipients;
    private String Object;
    private String text;
    private String Date;
    private boolean news = false;
    private int id;

    public Email(String sender, String recipients, String object, String text) {
        this.sender = sender;
        this.recipients = recipients;
        Object = object;
        this.text = text;
        Date = String.valueOf(new Date());
    }

    public Email() {
    }

    public Email(String sender, String recipients, String object, String text, String date, int id) {
        this.sender = sender;
        this.recipients = recipients;
        this.Object = object;
        this.text = text;
        this.Date = date;
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipients() {
        return recipients;
    }

    public String getObject() {
        return Object;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return Date;
    }

    public boolean getNews() {
        return news;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public void setNews(boolean news) {
        this.news = news;
    }

    @Override
    public String toString() {
        return sender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Email email = (Email) o;

        if (!Objects.equals(sender, email.sender)) return false;
        if (!Objects.equals(recipients, email.recipients)) return false;
        if (!Objects.equals(Object, email.Object)) return false;
        if (!Objects.equals(text, email.text)) return false;
        return Objects.equals(Date, email.Date);
    }


}
