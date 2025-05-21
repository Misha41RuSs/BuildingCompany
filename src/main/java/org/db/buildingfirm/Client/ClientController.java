package org.db.buildingfirm.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javax.swing.text.html.ImageView;
import java.io.IOException;

public class ClientController {

    @FXML private Button logoutNavButton;
    @FXML private Button makeOrderButton;
    @FXML private Button myOrdersButton;
    @FXML private Button viewReportsButton;

    @FXML
    private void viewReports(MouseEvent event) {

    }

    @FXML
    void makeOrder(MouseEvent event) {

    }

    @FXML
    void showOrders(MouseEvent event) {

    }

    public void logout(ActionEvent event) {
        try {
            Stage stage = (Stage) logoutNavButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/db/buildingfirm/FXML/Login.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Авторизация");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
