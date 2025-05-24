package org.db.buildingfirm.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.db.buildingfirm.DatabaseConnection;

import java.sql.*;

public class ChooseForemanController {

    @FXML private TableView<Worker> workerTable;
    @FXML private TableColumn<Worker, Integer> colId;
    @FXML private TableColumn<Worker, String> colName;

    private final ObservableList<Worker> workers = FXCollections.observableArrayList();
    private Worker selectedWorker;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        colName.setCellValueFactory(cell -> cell.getValue().fullNameProperty());

        loadWorkers();
        workerTable.setItems(workers);

        workerTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> selectedWorker = newSel
        );
    }

    private void loadWorkers() {
        workers.clear();
        String sql = """
            SELECT ID_рабочего, CONCAT(Фамилия, ' ', Имя, ' ', Отчество) AS ФИО
            FROM Рабочий
            WHERE ID_рабочего NOT IN (
                SELECT ID_главного_рабочего FROM Бригада WHERE ID_главного_рабочего IS NOT NULL
            )
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                workers.add(new Worker(rs.getInt("ID_рабочего"), rs.getString("ФИО")));
            }

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка загрузки рабочих: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void confirmSelection() {
        if (selectedWorker == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите рабочего").showAndWait();
            return;
        }

        // сохранить выбранного и закрыть
        ((Stage) workerTable.getScene().getWindow()).close();
    }

    public Worker getSelectedWorker() {
        return selectedWorker;
    }

    public Integer getSelectedWorkerId() {
        return selectedWorker != null ? selectedWorker.getId() : null;
    }

}
