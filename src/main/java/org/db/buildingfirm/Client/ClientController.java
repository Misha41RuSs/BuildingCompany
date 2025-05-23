package org.db.buildingfirm.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientController {

    @FXML private ImageView clientPhoto;
    @FXML private Button    logoutNavButton;
    @FXML private Button    makeOrderButton;
    @FXML private Button    myOrdersButton;
    @FXML private Button    viewReportsButton;

    private int currentClientId;
    public void setCurrentClientId(int id) {
        this.currentClientId = id;
    }

    @FXML
    void makeOrder(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/org/db/buildingfirm/FXML/makeOrder.fxml"));
        Parent root = loader.load();

        MakeOrderController ctrl = loader.getController();
        ctrl.setCurrentUserId(currentClientId);

        Stage st = (Stage)((Node)event.getSource()).getScene().getWindow();
        st.setScene(new Scene(root));
        st.setTitle("Выбор дома");
        st.show();
    }

    @FXML
    void showOrders(ActionEvent event) throws IOException {
        // TODO: реализация «Мои заказы»
        // 1) Загружаем FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/db/buildingfirm/FXML/MyOrders.fxml"));
        Parent root = loader.load();

        // 2) Получаем контроллер и передаём в него ID клиента
        MyOrdersController ctrl = loader.getController();
        ctrl.setCurrentClientId(currentClientId);

        // 3) Меняем сцену
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Мои заказы");
        stage.show();
    }

    @FXML
    void viewReports(ActionEvent event) {
        // TODO: реализация «Статистика заказов»
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/org/db/buildingfirm/FXML/Login.fxml"));
        Parent root = loader.load();
        Stage st = (Stage)logoutNavButton.getScene().getWindow();
        st.setScene(new Scene(root));
        st.setTitle("Авторизация");
        st.show();
    }
}
