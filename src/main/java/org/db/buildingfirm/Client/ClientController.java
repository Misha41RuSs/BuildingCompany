package org.db.buildingfirm.Client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientController {

    @FXML
    private Button logoutButton;

    @FXML
    private Button makeOrdButt;

    @FXML
    private Button myOrders;

    @FXML
    private Button viewReportsButton;

    @FXML
    private void viewReports(MouseEvent event) {

    }

    @FXML
    void makeOrder(MouseEvent event) {

    }

    @FXML
    void showOrders(MouseEvent event) {

    }

    @FXML
    private void logout(MouseEvent event) {
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
