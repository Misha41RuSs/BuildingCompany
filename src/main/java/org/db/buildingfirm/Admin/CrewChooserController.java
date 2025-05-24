package org.db.buildingfirm.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.db.buildingfirm.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CrewChooserController {

    @FXML
    private ComboBox<CrewItem> crewCombo;

    private final ObservableList<CrewItem> crews = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadCrews();
        crewCombo.setItems(crews);
    }

    private void loadCrews() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ID_бригады, Наименование FROM Бригада");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                crews.add(new CrewItem(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Integer getSelectedCrewId() {
        CrewItem selected = crewCombo.getValue();
        return selected != null ? selected.getId() : null;
    }

    @FXML
    private void confirm() {
        Stage stage = (Stage) crewCombo.getScene().getWindow();
        stage.close();
    }

    public static class CrewItem {
        private final int id;
        private final String name;

        public CrewItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
