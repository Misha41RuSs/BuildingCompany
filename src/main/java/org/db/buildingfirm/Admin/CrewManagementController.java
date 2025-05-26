package org.db.buildingfirm.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.db.buildingfirm.DTO.Crew;
import org.db.buildingfirm.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CrewManagementController {

    @FXML private TableView<Crew> tableCrews;
    @FXML private TableColumn<Crew, Integer> colId;
    @FXML private TableColumn<Crew, String> colName;
    @FXML private TableColumn<Crew, String> colForeman;

    private final ObservableList<Crew> crews = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        colName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        colForeman.setCellValueFactory(cell -> cell.getValue().foremanProperty());

        tableCrews.setItems(crews);
        loadCrews();
    }

    private void loadCrews() {
        crews.clear();
        String sql = """
            SELECT б.ID_бригады, б.Наименование,
                   CONCAT(р.Фамилия, ' ', р.Имя, ' ', р.Отчество) AS Главный
            FROM Бригада б
            LEFT JOIN Рабочий р ON б.ID_главного_рабочего = р.ID_рабочего
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                crews.add(new Crew(
                        rs.getInt("ID_бригады"),
                        rs.getString("Наименование"),
                        rs.getString("Главный")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка загрузки данных: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void assignForeman(ActionEvent event) {
        Crew selectedCrew = tableCrews.getSelectionModel().getSelectedItem();
        if (selectedCrew == null) {
            showWarning("Выберите бригаду");
            return;
        }

        try {
            // Загружаем модальное окно выбора рабочего
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/ChooseForeman.fxml"));
            Parent root = loader.load();
            ChooseForemanController controller = loader.getController();

            // Показываем окно
            Stage dialog = new Stage();
            dialog.setTitle("Назначить главного рабочего");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

            // Получаем выбранного рабочего
            Integer selectedWorkerId = controller.getSelectedWorkerId();
            if (selectedWorkerId == null) {
                return;
            }

            // Проверяем, не назначен ли он уже главным в другой бригаде
            String checkSql = "SELECT COUNT(*) FROM Бригада WHERE ID_главного_рабочего = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

                checkStmt.setInt(1, selectedWorkerId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showWarning("Этот рабочий уже является главным в другой бригаде");
                    return;
                }
            }

            // Назначаем главного рабочего
            String updateSql = "UPDATE Бригада SET ID_главного_рабочего = ? WHERE ID_бригады = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

                updateStmt.setInt(1, selectedWorkerId);
                updateStmt.setInt(2, selectedCrew.getId());
                updateStmt.executeUpdate();
            }

            loadCrews();
            showInfo("Главный рабочий успешно назначен");

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showError("Ошибка при назначении главного рабочего: " + e.getMessage());
        }
    }


    @FXML
    void sendToProject(ActionEvent event) {
        Crew selected = tableCrews.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите бригаду");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/AssignOrder.fxml"));
            Parent root = loader.load();
            AssignToOrderController ctrl = loader.getController();
            ctrl.setCrewId(selected.getId());

            Stage dlg = new Stage();
            dlg.setScene(new Scene(root));
            dlg.setTitle("Выбрать объект строительства");
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.showAndWait();

            loadCrews();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка при отправке на объект: " + e.getMessage());
        }
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/Admin.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Панель администратора");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Не удалось вернуться назад").showAndWait();
        }
    }


    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
}
