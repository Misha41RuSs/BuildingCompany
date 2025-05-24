package org.db.buildingfirm.Admin;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;

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

    private String currentUserLogin;

    public void setCurrentUserLogin(String login) {
        this.currentUserLogin = login;
    }


    @FXML
    void showStatistics(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/db/buildingfirm/FXML/ReportAdmin.fxml"));
        Scene scene = new Scene(loader.load());

        ReportAdminController reportCtrl = loader.getController();
        reportCtrl.setAdminLogin(currentUserLogin);

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Отчеты по зданиям");
        stage.centerOnScreen();
        stage.show();
    }



    @FXML
    void showCrews(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/CrewManagement.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Управление бригадами");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Не удалось загрузить форму управления бригадами").showAndWait();
        }
    }



    @FXML
    void showEmployees(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/db/buildingfirm/FXML/AssignCrew.fxml"));
        Scene scene = new Scene(loader.load());

// Правильный каст
        AssignCrewController assignCtrl = loader.getController();
// Если тебе нужно передать логин, сделай метод setAdminLogin в AssignCrewController
// assignCtrl.setAdminLogin(currentUserLogin); ← если нужно

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Назначение в бригады");
        stage.centerOnScreen();
        stage.show();

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
