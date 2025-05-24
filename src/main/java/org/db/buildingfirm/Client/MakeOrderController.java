package org.db.buildingfirm.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.db.buildingfirm.DatabaseConnection;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.Optional;

public class MakeOrderController {
    @FXML private TextField priceField;
    @FXML private Button confirmButton;

    // Шаблоны расхода материалов по типам домов
    private final Map<String, Map<Integer,Integer>> templates = Map.of(
            "Одноэтажный", Map.of(1,100,  2,50,  3,200),
            "Двухэтажный", Map.of(1,200,  2,80,  3,400),
            "С террасой",   Map.of(1,300,  2,120, 3,600)
    );

    // Ориентировочные сроки и цены
    private final Map<String, Integer>   leadTimes  = Map.of(
            "Одноэтажный", 30,
            "Двухэтажный", 45,
            "С террасой",   60
    );
    private final Map<String, Double>    estimates  = Map.of(
            "Одноэтажный", 1_000_000.0,
            "Двухэтажный", 1_800_000.0,
            "С террасой",   2_500_000.0
    );

    @FXML private Button backButton;
    @FXML private Button orderOneStory;
    @FXML private Button orderTwoStory;
    @FXML private Button orderMansion;

    private int currentUserId;

    // Текущий выбранный тип дома — нужен в confirmOrder()
    private String currentHouseType;

    /** Вызывается из LoginController сразу после загрузки формы */
    public void setCurrentUserId(int id) {
        this.currentUserId = id;
    }

    @FXML
    void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/db/buildingfirm/FXML/Client.fxml"));
        Parent root = loader.load();
        ClientController ctrl = loader.getController();
        ctrl.setCurrentClientId(currentUserId);

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Клиент");
        stage.show();
    }

    @FXML
    void orderOneStory(ActionEvent event) {
        showConfirmDialog("Одноэтажный");
    }

    @FXML
    void orderTwoStory(ActionEvent event) {
        showConfirmDialog("Двухэтажный");
    }

    @FXML
    void orderMansion(ActionEvent event) {
        showConfirmDialog("С террасой");
    }

    /**
     * Показывает модальный диалог с оценкой, сроками и условиями.
     * Если пользователь нажал «Оплатить» — вызывает confirmOrder().
     */
    private void showConfirmDialog(String houseType) {
        this.currentHouseType = houseType;
        double estimate = estimates.get(houseType);
        int days = leadTimes.get(houseType);

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Подтверждение заказа");
        dlg.setHeaderText("Вы выбрали: " + houseType);

        ButtonType confirm = new ButtonType("Подтвердить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel  = new ButtonType("Отменить", ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(confirm, cancel);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Срок выполнения (дней):"), 0, 0);
        grid.add(new Label(String.valueOf(days)), 1, 0);
        grid.add(new Label("Смета (₽):"), 0, 1);
        TextField dialogPrice = new TextField(String.valueOf(estimate));
        grid.add(dialogPrice, 1, 1);
        grid.add(new Label("Условия:"), 0, 2);
        grid.add(new Label("50% аванс, остальное при сдаче"), 1, 2);
        grid.add(new Label("Адрес объекта:"), 0, 3);
        TextField addressField = new TextField();
        grid.add(addressField, 1, 3);
        dlg.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dlg.showAndWait();
        if (res.isPresent() && res.get() == confirm) {
            priceField.setText(dialogPrice.getText());
            priceField.setVisible(true);
            priceField.setManaged(true);
            confirmButton.setVisible(true);
            confirmButton.setManaged(true);

            confirmButton.setOnAction(ev -> {
                try {
                    double price = Double.parseDouble(dialogPrice.getText());
                    confirmOrder(ev, addressField.getText(), price);
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Некорректная цена").showAndWait();
                }
            });
        }
    }

    private void confirmOrder(ActionEvent event, String address, double price) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlOrder = """
                INSERT INTO Заказ (ID_пользователя, Тип_дома, Статус, Дата_создания, Адрес_объекта)
                VALUES (?, ?, 'В обработке', NOW(), ?)
            """;
            PreparedStatement psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, currentUserId);
            psOrder.setString(2, currentHouseType);
            psOrder.setString(3, address);
            psOrder.executeUpdate();

            ResultSet rsKeys = psOrder.getGeneratedKeys();
            if (!rsKeys.next()) throw new SQLException("Не удалось получить ID заказа");
            int orderId = rsKeys.getInt(1);

            Map<Integer,Integer> needs = templates.get(currentHouseType);
            String sqlMat = """
                INSERT INTO Расход_материалов
                  (Количество, Стоимость, ID_здания, ID_материала, ID_заказа)
                VALUES (?, ?, NULL, ?, ?)
            """;

            for (var entry : needs.entrySet()) {
                int matId = entry.getKey(), qty = entry.getValue();
                double unitCost = fetchUnitCost(conn, matId);

                try (PreparedStatement psMat = conn.prepareStatement(sqlMat)) {
                    psMat.setInt(1, qty);
                    psMat.setDouble(2, qty * unitCost);
                    psMat.setInt(3, matId);
                    psMat.setInt(4, orderId);
                    psMat.executeUpdate();
                }
            }

            new Alert(Alert.AlertType.INFORMATION,
                    "Заказ #" + orderId + " успешно создан и в обработке")
                    .showAndWait();

        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Ошибка оформления заказа:\n" + ex.getMessage())
                    .showAndWait();
        }
    }


    /**
     * Делает фактическую вставку в БД: заказ + расход_материалов.
     */
    @FXML
    private void confirmOrder(double price) {

    }

    /** Получаем цену за единицу материала из таблицы Материал */
    private double fetchUnitCost(Connection conn, int matId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT Стоимость FROM Материал WHERE ID_материала = ?")) {
            ps.setInt(1, matId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
            throw new SQLException("Материал не найден: " + matId);
        }
    }


    public void confirmOrder(ActionEvent event) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1) создаём заказ и получаем его ID
            String sqlOrder = """
            INSERT INTO Заказ (ID_пользователя, Тип_дома, Статус, Дата_создания)
            VALUES (?, ?, 'В обработке', NOW())
        """;
            PreparedStatement psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, currentUserId);
            psOrder.setString(2, currentHouseType);
            psOrder.executeUpdate();

            ResultSet rsKeys = psOrder.getGeneratedKeys();
            if (!rsKeys.next()) throw new SQLException("Не удалось получить ID заказа");
            int orderId = rsKeys.getInt(1);

            // 2) записываем расход материалов по шаблону, включая ID_заказа
            Map<Integer,Integer> needs = templates.get(currentHouseType);
            String sqlMat = """
            INSERT INTO Расход_материалов
              (Количество, Стоимость, ID_здания, ID_материала, ID_заказа)
            VALUES (?, ?, NULL, ?, ?)
        """;
            for (var entry : needs.entrySet()) {
                int matId = entry.getKey(), qty = entry.getValue();
                double unitCost = fetchUnitCost(conn, matId);

                try (PreparedStatement psMat = conn.prepareStatement(sqlMat)) {
                    psMat.setInt(1, qty);
                    psMat.setDouble(2, qty * unitCost);
                    psMat.setInt(3, matId);
                    psMat.setInt(4, orderId);       // вот здесь мы передаём ID_заказа
                    psMat.executeUpdate();
                }
            }

            new Alert(Alert.AlertType.INFORMATION,
                    "Заказ #" + orderId + " успешно создан и в обработке")
                    .showAndWait();

        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Ошибка оформления заказа:\n" + ex.getMessage())
                    .showAndWait();
        }
    }
}
