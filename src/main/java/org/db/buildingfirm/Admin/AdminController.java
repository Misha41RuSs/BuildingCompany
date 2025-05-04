package org.db.buildingfirm.Admin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class AdminController {

    @FXML
    private Button crew;

    @FXML
    private Button employees;

    @FXML
    private Button logoutButton;

    @FXML
    private Button statisticsButton;


    @FXML
    void showStatistics(MouseEvent event) {
        try {
            Stage stage = (Stage) statisticsButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/ReportAdmin.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Отчеты по зданиям");
            stage.centerOnScreen(); // Центрирование окна
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void showCrews(MouseEvent event) {

    }

    @FXML
    void showEmployees(MouseEvent event) {

    }


    // В методе выхода из аккаунта
    @FXML
    private void logout() {
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
