module com.example.prog3esame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;


    opens Server to javafx.fxml;
    exports Server;
    opens Client to javafx.fxml;
    exports Client;
    exports CommonUtils;
    opens CommonUtils to javafx.fxml;
}