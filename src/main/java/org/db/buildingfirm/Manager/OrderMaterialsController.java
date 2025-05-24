package org.db.buildingfirm.Manager;

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

public class OrderMaterialsController {

    @FXML private TableView<MaterialItem> materialsTable;
    @FXML private TableColumn<MaterialItem, Integer> colMatId;
    @FXML private TableColumn<MaterialItem, String>  colName;
    @FXML private TableColumn<MaterialItem, String>  colUnit;
    @FXML private TableColumn<MaterialItem, Integer> colStock;

    @FXML private TextField qtyField;
    @FXML private Button    orderButton;

    private final ObservableList<MaterialItem> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colMatId .setCellValueFactory(cd -> cd.getValue().idProperty().asObject());
        colName  .setCellValueFactory(cd -> cd.getValue().nameProperty());
        colUnit  .setCellValueFactory(cd -> cd.getValue().unitProperty());
        colStock .setCellValueFactory(cd -> cd.getValue().stockProperty().asObject());

        materialsTable.setItems(data);
        loadMaterials();

        // Блокируем кнопку заказа пока ничего не выбрано
        orderButton.setDisable(true);
        materialsTable.getSelectionModel().selectedItemProperty()
                .addListener((o,old,sel) -> orderButton.setDisable(sel == null));
    }

    /** Загружает все материалы с текущими остатками из Inventory */
    private void loadMaterials() {
        String sql =
                """
                SELECT m.ID_материала,
                       m.Наименование,
                       m.Единица_измерения,
                       COALESCE(i.Количество_на_складе, 0) AS stock
                  FROM Материал m
                  LEFT JOIN Inventory i
                    ON m.ID_материала = i.ID_материала
                 ORDER BY m.ID_материала
                """;

        data.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                data.add(new MaterialItem(
                        rs.getInt("ID_материала"),
                        rs.getString("Наименование"),
                        rs.getString("Единица_измерения"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Не удалось загрузить материалы:\n" + ex.getMessage())
                    .showAndWait();
        }
    }

    /** Оформление заказа у поставщика + апдейт остатков на складе */
    @FXML
    void makeOrder(ActionEvent ev) {
        MaterialItem sel = materialsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите материал").showAndWait();
            return;
        }

        String txt = qtyField.getText().trim();
        if (txt.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Введите количество").showAndWait();
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(txt);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Неверный формат количества").showAndWait();
            return;
        }

        String sqlOrder = """
        INSERT INTO Заказ_поставщику
            (ID_материала, Количество, Дата_создания)
        VALUES (?, ?, NOW())
    """;

        String sqlSupply = """
        INSERT INTO Поставка_материала
            (ID_материала, Количество, Дата)
        VALUES (?, ?, CURDATE())
    """;

        String sqlUpdate = """
        INSERT INTO Inventory (ID_материала, Количество_на_складе)
        VALUES (?, ?)
        ON DUPLICATE KEY UPDATE
            Количество_на_складе = Количество_на_складе + VALUES(Количество_на_складе)
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psOrder  = conn.prepareStatement(sqlOrder);
             PreparedStatement psSupply = conn.prepareStatement(sqlSupply);
             PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {

            conn.setAutoCommit(false);

            psOrder.setInt(1, sel.getId());
            psOrder.setInt(2, qty);
            psOrder.executeUpdate();

            psSupply.setInt(1, sel.getId());
            psSupply.setInt(2, qty);
            psSupply.executeUpdate();

            psUpdate.setInt(1, sel.getId());
            psUpdate.setInt(2, qty);
            psUpdate.executeUpdate();

            conn.commit();

            new Alert(Alert.AlertType.INFORMATION,
                    "Материал «" + sel.getName() + "» заказан и поставлен в количестве " + qty)
                    .showAndWait();

            loadMaterials(); // обновим таблицу остатков

        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Не удалось оформить заказ:\n" + ex.getMessage()).showAndWait();
        }
    }


    @FXML
    public void goBack(ActionEvent event) throws IOException {
        // 1. Загружаем FXML менеджера
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/db/buildingfirm/FXML/Manager.fxml")
        );
        Parent root = loader.load();

        // 2. Достаём текущую Stage из источника события
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        // 3. Устанавливаем новую Scene и заголовок
        stage.setScene(new Scene(root));
        stage.setTitle("Менеджер");
        stage.show();
    }
}
