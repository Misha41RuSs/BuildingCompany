package org.db.buildingfirm;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.sql.*;

public class LoginController {

    @FXML private Button loginButton;        // правая кнопка «Авторизоваться»
    @FXML private TextField loginField;
    @FXML private TextField passwordField;

    // левые кнопки навигации
    @FXML private Button sideLoginButton;    // fx:id="sideLoginButton"
    @FXML private Button registerButton;     // fx:id="registerButton"

    @FXML
    public void initialize() {
        // При старте отмечаем «Войти» как активную
        sideLoginButton.getStyleClass().add("active");
        // Обработчик для кнопки «Регистрация»
        registerButton.setOnMouseClicked(this::openRegister);
    }

    @FXML
    void auth(MouseEvent event) {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (authenticateUser(login, password)) {
            String role = getRole(login);
            loadNewScene(role);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка авторизации");
            alert.setHeaderText("Неверный логин или пароль");
            alert.showAndWait();
        }
    }

    private boolean authenticateUser(String login, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Пользователь WHERE Логин = ? AND Пароль = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, login);
                ps.setString(2, password);
                return ps.executeQuery().next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getRole(String login) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT r.Наименование FROM Пользователь p JOIN Роль r ON p.ID_роли = r.ID_роли WHERE p.Логин = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, login);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("Наименование");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadNewScene(String role) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            String fxmlFile = "";
            String fxmlTitle = "";

            switch (role) {
                case "Admin":
                    fxmlFile = "Admin.fxml"; fxmlTitle = "Панель админа"; break;
                case "Manager":
                    fxmlFile = "Manager.fxml"; fxmlTitle = "Панель менеджера"; break;
                case "Client":
                    fxmlFile = "Client.fxml"; fxmlTitle = "Панель клиента"; break;
                default:
                    new Alert(Alert.AlertType.ERROR, "Роль не найдена").showAndWait();
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/org/db/buildingfirm/FXML/" + fxmlFile));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle(fxmlTitle);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openRegister(MouseEvent event) {
        try {
            // Переключаем стиль кнопок
            sideLoginButton.getStyleClass().remove("active");
            registerButton.getStyleClass().add("active");

            // Загружаем сцену регистрации
            Stage stage = (Stage) sideLoginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/org/db/buildingfirm/FXML/Register.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Регистрация");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
