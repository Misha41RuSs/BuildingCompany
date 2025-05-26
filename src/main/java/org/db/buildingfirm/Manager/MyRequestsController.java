package org.db.buildingfirm.Manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.db.buildingfirm.DTO.Request;
import org.db.buildingfirm.DatabaseConnection;

import java.io.IOException;
import java.sql.*;

public class MyRequestsController {

    @FXML private TableView<Request>  requestsTable;
    @FXML private TableColumn<Request,Integer> colId;
    @FXML private TableColumn<Request,String>  colLogin;
    @FXML private TableColumn<Request,String>  colType;
    @FXML private TableColumn<Request,String>  colStatus;
    @FXML private TableColumn<Request,String>  colCreated;

    @FXML private Button backButton;
    @FXML private Button detailsButton;

    private final ObservableList<Request> requests = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Настраиваем колонки
        colId.setCellValueFactory(cd -> cd.getValue().idProperty().asObject());
        colLogin.setCellValueFactory(cd -> cd.getValue().loginProperty());
        colType.setCellValueFactory(cd -> cd.getValue().typeProperty());
        colStatus.setCellValueFactory(cd -> cd.getValue().statusProperty());
        colCreated.setCellValueFactory(cd -> cd.getValue().createdProperty());

        // Привязываем список к таблице
        requestsTable.setItems(requests);

        // Кнопка детализации выключена пока нет выбора
        requestsTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, sel) -> detailsButton.setDisable(sel == null));

        loadRequests();
    }

    private void loadRequests() {
        requests.clear();
        String sql = """
            SELECT 
              o.ID_заказа,
              u.Логин      AS login,
              o.Тип_дома   AS type,
              o.Статус     AS status,
              DATE_FORMAT(o.Дата_создания, '%Y-%m-%d %H:%i:%s') AS created
            FROM Заказ o
            JOIN Пользователь u
              ON o.ID_пользователя = u.ID_пользователя
            WHERE o.Статус = 'В обработке'
            ORDER BY o.Дата_создания DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) {
                requests.add(new Request(
                        rs.getInt("ID_заказа"),
                        rs.getString("login"),
                        rs.getString("type"),
                        rs.getString("status"),
                        rs.getString("created")
                ));
            }
        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Не удалось загрузить заявки:\n" + ex.getMessage())
                    .showAndWait();
        }
    }

    @FXML
    void goBack(ActionEvent ev) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/db/buildingfirm/FXML/Manager.fxml"));
        Parent root = loader.load();
        Stage st = (Stage)((Node)ev.getSource()).getScene().getWindow();
        st.setScene(new Scene(root));
        st.setTitle("Менеджер");
        st.show();
    }

    @FXML
    void showDetails(ActionEvent ev) {
        Request rq = requestsTable.getSelectionModel().getSelectedItem();
        if (rq == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите заявку").showAndWait();
            return;
        }

        int orderId = rq.getId();

        StringBuilder sb = new StringBuilder()
                .append("Заявка #").append(orderId).append("\n")
                .append("Клиент: ").append(rq.getLogin()).append("\n")
                .append("Тип: ").append(rq.getType()).append("\n")
                .append("Статус: ").append(rq.getStatus()).append("\n")
                .append("Дата: ").append(rq.getCreated()).append("\n\n")
                .append("Материалы:\n");

        // Сразу считаем, что все есть
        boolean allInStock = true;

        String sqlMat = """
        SELECT rm.ID_материала,
               m.Наименование,
               rm.Количество
          FROM Расход_материалов rm
          JOIN Материал m
            ON rm.ID_материала = m.ID_материала
         WHERE rm.ID_заказа = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlMat)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int matId    = rs.getInt("ID_материала");
                    String name  = rs.getString("Наименование");
                    int needed   = rs.getInt("Количество");
                    boolean inStock = checkStock(conn, matId, needed);
                    if (!inStock) allInStock = false;

                    sb.append("• ")
                            .append(name)
                            .append(" — ")
                            .append(needed)
                            .append(inStock ? " (в наличии)\n" : " (недостаточно)\n");
                }
            }

        } catch (SQLException ex) {
            sb.append("\n(Ошибка загрузки материалов: ")
                    .append(ex.getMessage())
                    .append(")\n");
            allInStock = false;
        }

        // Строим диалог
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Информация по заявке");
        dlg.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Передать администратору", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CLOSE
        );
        dlg.getDialogPane().setContent(new Label(sb.toString()));

        // Заблокировать кнопку «Передать администратору», если чего-то недостаёт
        ButtonType okType = dlg.getDialogPane()
                .getButtonTypes()
                .stream()
                .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .findFirst().orElse(null);
        if (okType != null) {
            Button btn = (Button) dlg.getDialogPane().lookupButton(okType);
            btn.setDisable(!allInStock);
        }

        // Показываем и, если нажали OK, меняем статус
        dlg.showAndWait().ifPresent(bt -> {
            if (bt.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                transferToAdmin(orderId);
                loadRequests();  // обновить таблицу
            }
        });
    }
    private boolean checkStock(Connection conn, int materialId, int needed)
            throws SQLException {
        String sql = """
        SELECT Количество_на_складе
          FROM Inventory
         WHERE ID_материала = ?
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materialId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) >= needed;
                }
            }
        }
        // если нет записи в Inventory — считаем, что на складе нет
        return false;
    }


    /** Проверяем, хватает ли на складе нужного количества */
    private boolean checkStock(Connection conn, String matName, int needed)
            throws SQLException {
        String sql = "SELECT Количество_на_складе FROM Inventory WHERE Наименование = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) >= needed;
                }
            }
        }
        return false;
    }

    /** Меняем статус заявки на «Готово к передаче» */
    /**
     * Обновляем статус заявки на «Формируются бригады»
     */
    private void transferToAdmin(int orderId) {
        String sql = "UPDATE Заказ SET Статус = 'Формируются бригады' WHERE ID_заказа = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
            new Alert(Alert.AlertType.INFORMATION,
                    "Заявка #"+orderId+" передана администратору и статус изменён на «Формируются бригады»")
                    .showAndWait();
        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Не удалось передать заявку:\n"+ex.getMessage())
                    .showAndWait();
        }
    }

}
