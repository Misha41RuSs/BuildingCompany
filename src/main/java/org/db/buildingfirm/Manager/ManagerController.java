package org.db.buildingfirm.Manager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

public class ManagerController {

    @FXML
    private Button clientsButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button orderMatButton;

    @FXML
    private Button processButton;

    @FXML
    private Button supplyMaterials;


    @FXML
    void showBasicReport(ActionEvent event) {
        try {
            // Переключаем сцену на форму отчета
            Stage stage = (Stage) supplyMaterials.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/ReportManager.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Отчет по поставкам материалов");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void showSupplyReport(ActionEvent event) {
        try {
            // Переключаем сцену на форму отчета
            Stage stage = (Stage) supplyMaterials.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/ReportSupply.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Отчет по поставкам материалов");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void orderMaterials(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/db/buildingfirm/FXML/OrderMaterials.fxml"));
        Parent root = loader.load();
        Stage st = (Stage)((Node)event.getSource()).getScene().getWindow();
        st.setScene(new Scene(root));
        st.setTitle("Заказ материалов");
        st.show();
    }


    @FXML
    void showClients(ActionEvent event) throws IOException {
        // 1) Загружаем FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/db/buildingfirm/FXML/MyRequests.fxml"));
        Parent root = loader.load();

        // 2) (Опционально) передайте в MyRequestsController какие-нибудь параметры,
        //    например ID менеджера, если он нужен:
        // MyRequestsController ctrl = loader.getController();
        // ctrl.setCurrentManagerId(this.currentManagerId);

        // 3) Поменяем сцену
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Заявки от клиентов");
        stage.show();
    }


    @FXML
    void showProcessClients(MouseEvent event) {

    }

    @FXML
    public void logout(ActionEvent event) {
        try {
            // Переключаем сцену на экран логина
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/Login.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Авторизация");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
