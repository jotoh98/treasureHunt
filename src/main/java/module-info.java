module hunt {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.locationtech.jts;
    requires reflections;
    requires java.desktop;
    requires org.slf4j;
    opens com.treasure.hunt;
    exports com.treasure.hunt;
    exports com.treasure.hunt.view.javafx;
}