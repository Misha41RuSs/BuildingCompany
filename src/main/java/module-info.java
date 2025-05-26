module org.db.buildingfirm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.desktop;


    exports org.db.buildingfirm;
    opens org.db.buildingfirm to javafx.fxml;  // Открываем пакет для javafx.fxml
    opens org.db.buildingfirm.Admin to javafx.fxml;
    opens org.db.buildingfirm.Client to javafx.fxml;
    opens org.db.buildingfirm.Manager to javafx.fxml;
    opens org.db.buildingfirm.DTO to javafx.base, javafx.fxml;
    opens org.db.buildingfirm.Utilities to javafx.base, javafx.fxml;

}
