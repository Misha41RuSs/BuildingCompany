package org.db.buildingfirm.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.db.buildingfirm.DTO.Worker;
import org.db.buildingfirm.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AssignCrewController {

    @FXML private TableView<Worker> tableWorkers;
    @FXML private TableColumn<Worker, Integer> colId;
    @FXML private TableColumn<Worker, String> colFullName;
    @FXML private TableColumn<Worker, Integer> colExperience;
    @FXML private TableColumn<Worker, String> colPhone;
    @FXML private TableColumn<Worker, String> colAddress;
    @FXML private TableColumn<Worker, String> colSpecialty;
    @FXML private Button backButton;
    @FXML private Button assignButton;
    @FXML private Button removeButton;

    private final ObservableList<Worker> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        colFullName.setCellValueFactory(w -> w.getValue().fullNameProperty());
        colExperience.setCellValueFactory(cell -> cell.getValue().experienceProperty().asObject());
        colPhone.setCellValueFactory(cell -> cell.getValue().phoneProperty());
        colAddress.setCellValueFactory(cell -> cell.getValue().addressProperty());
        colSpecialty.setCellValueFactory(cell -> cell.getValue().specialtyProperty());

        tableWorkers.setItems(data);
        loadWorkers();
    }

    private void loadWorkers() {
        data.clear();
        String sql = """
            SELECT р.ID_рабочего, CONCAT(р.Фамилия, ' ', р.Имя, ' ', р.Отчество) AS ФИО,
                   р.Стаж, р.Номер_телефона, р.Адрес_проживания,
                   GROUP_CONCAT(с.Наименование SEPARATOR ', ') AS Специальности
            FROM Рабочий р
            LEFT JOIN Специализация_рабочего ср ON р.ID_рабочего = ср.ID_рабочего
            LEFT JOIN Специальность с ON ср.ID_специальности = с.ID_специальности
            GROUP BY р.ID_рабочего
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.add(new Worker(
                        rs.getInt("ID_рабочего"),
                        rs.getString("ФИО"),
                        rs.getInt("Стаж"),
                        rs.getString("Номер_телефона"),
                        rs.getString("Адрес_проживания"),
                        rs.getString("Специальности")
                ));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка при загрузке данных: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void assignToCrew(ActionEvent event) {
        Worker selected = tableWorkers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите рабочего").showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/CrewChooser.fxml"));
            Parent root = loader.load();
            CrewChooserController ctrl = loader.getController();

            Stage dlg = new Stage();
            dlg.setScene(new Scene(root));
            dlg.setTitle("Выбор бригады");
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.showAndWait();

            Integer crewId = ctrl.getSelectedCrewId();
            if (crewId == null) return;

            String insert = "INSERT INTO Состав_бригады (ID_рабочего, ID_бригады) VALUES (?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, selected.getId());
                ps.setInt(2, crewId);
                ps.executeUpdate();

                new Alert(Alert.AlertType.INFORMATION, "Рабочий назначен в бригаду").showAndWait();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка назначения в бригаду: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void removeFromCrew(ActionEvent event) {
        Worker selected = tableWorkers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите рабочего").showAndWait();
            return;
        }

        String delete = "DELETE FROM Состав_бригады WHERE ID_рабочего = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(delete)) {
            ps.setInt(1, selected.getId());
            ps.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Рабочий удален из бригады").showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка удаления: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/Admin.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Администратор");
        stage.show();
    }
}
