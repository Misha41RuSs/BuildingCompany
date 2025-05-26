package org.db.buildingfirm.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.db.buildingfirm.DTO.OrderItem;
import org.db.buildingfirm.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AssignToOrderController {

    @FXML private TableView<OrderItem> tableOrders;
    @FXML private TableColumn<OrderItem, Integer> colOrderId;
    @FXML private TableColumn<OrderItem, String> colAddress;

    @FXML private Button assignButton;

    private final ObservableList<OrderItem> orderList = FXCollections.observableArrayList();
    private Integer crewId;

    public void setCrewId(int crewId) {
        this.crewId = crewId;
        loadOrders();
    }

    private void loadOrders() {
        orderList.clear();
        String query = "SELECT ID_заказа, Адрес_объекта FROM Заказ WHERE ID_бригады IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orderList.add(new OrderItem(
                        rs.getInt("ID_заказа"),
                        rs.getString("Адрес_объекта")
                ));
            }

            colOrderId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
            colAddress.setCellValueFactory(data -> data.getValue().addressProperty());

            tableOrders.setItems(orderList);

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка загрузки заказов: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void assignToOrder(ActionEvent event) {
        OrderItem selected = tableOrders.getSelectionModel().getSelectedItem();
        if (selected == null || crewId == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите заказ").showAndWait();
            return;
        }

        String update = "UPDATE Заказ SET ID_бригады = ? WHERE ID_заказа = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setInt(1, crewId);
            ps.setInt(2, selected.getId());
            ps.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Бригада назначена на объект").showAndWait();
            ((Stage) assignButton.getScene().getWindow()).close();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка назначения: " + e.getMessage()).showAndWait();
        }
    }
}
