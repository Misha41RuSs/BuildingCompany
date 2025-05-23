package org.db.buildingfirm.Client;

import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.db.buildingfirm.DatabaseConnection;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MyOrdersController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order,Integer> colId;
    @FXML private TableColumn<Order,String>  colType;
    @FXML private TableColumn<Order,String>  colStatus;
    @FXML private TableColumn<Order,String>  colCreated;

    @FXML private Button backButton;
    @FXML private Button detailsButton;

    private int currentClientId;
    private final ObservableList<Order> orders = FXCollections.observableArrayList();

    public void setCurrentClientId(int id) {
        this.currentClientId = id;
        loadOrdersFromDb();
    }

    @FXML public void initialize() {
        colId.setCellValueFactory(cd -> cd.getValue().idProperty().asObject());
        colType.setCellValueFactory(cd -> cd.getValue().typeProperty());
        colStatus.setCellValueFactory(cd -> cd.getValue().statusProperty());
        colCreated.setCellValueFactory(cd -> cd.getValue().createdProperty());

        ordersTable.setItems(orders);
        ordersTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((o, old, sel) -> detailsButton.setDisable(sel == null));
    }

    @FXML void goBack(ActionEvent ev) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/db/buildingfirm/FXML/Client.fxml"));
        Parent root = loader.load();
        ClientController ctrl = loader.getController();
        ctrl.setCurrentClientId(currentClientId);

        Stage st = (Stage)((Node)ev.getSource()).getScene().getWindow();
        st.setScene(new Scene(root));
        st.setTitle("Клиент");
        st.show();
    }

    private void loadOrdersFromDb() {
        orders.clear();
        String sql = """
            SELECT ID_заказа, Тип_дома, Статус, Дата_создания
              FROM Заказ
             WHERE ID_пользователя = ?
             ORDER BY Дата_создания DESC
        """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, currentClientId);
            ResultSet rs = ps.executeQuery();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("ID_заказа"),
                        rs.getString("Тип_дома"),
                        rs.getString("Статус"),
                        rs.getTimestamp("Дата_создания")
                                .toLocalDateTime()
                                .format(fmt)
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Не удалось загрузить заказы:\n" + ex.getMessage())
                    .showAndWait();
        }
    }

    @FXML
    void showDetails(ActionEvent ev) {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Пожалуйста, выберите заказ.").showAndWait();
            return;
        }

        int    orderId = selected.getId();
        String type    = selected.getType();
        String status  = selected.getStatus();
        String created = selected.getCreated(); // строка "yyyy-MM-dd HH:mm:ss"

        // Парсим строку обратно в LocalDateTime с тем же шаблоном
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dt;
        try {
            dt = LocalDateTime.parse(created, fmt);
        } catch (DateTimeParseException ex) {
            // если вдруг не смогли распарсить — просто оставим строку
            dt = null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Заказ #").append(orderId)
                .append(", Тип: ").append(type).append("\n")
                .append("Статус: ").append(status).append("\n");
        if (dt != null) {
            sb.append("Дата: ").append(dt.format(fmt)).append("\n\n");
        } else {
            sb.append("Дата: ").append(created).append("\n\n");
        }
        sb.append("Материалы:\n");

        String sql = """
        SELECT m.Наименование, rm.Количество
          FROM Расход_материалов rm
          JOIN Материал m   ON rm.ID_материала = m.ID_материала
         WHERE rm.ID_заказа = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sb.append("• ")
                            .append(rs.getString("Наименование"))
                            .append(" — количество ")
                            .append(rs.getInt("Количество"))
                            .append("\n");
                }
            }
        } catch (SQLException ex) {
            sb.append("\n(Ошибка загрузки материалов: ").append(ex.getMessage()).append(")");
        }

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Детали заказа");
        info.setHeaderText(null);
        info.setContentText(sb.toString());
        info.showAndWait();
    }

}
