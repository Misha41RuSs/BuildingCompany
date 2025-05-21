package org.db.buildingfirm.Manager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    void showSupplyReport(MouseEvent event) {
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
    void orderMaterials(MouseEvent event) {

    }

    @FXML
    void showClients(MouseEvent event) {

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
