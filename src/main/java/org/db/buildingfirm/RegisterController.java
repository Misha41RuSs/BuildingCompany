package org.db.buildingfirm;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController {

    @FXML private Button sideLoginButton;
    @FXML private Button registerButton;

    @FXML private TextField loginField;

    @FXML private PasswordField passwordField;
    @FXML private TextField    passwordVisibleField;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField    confirmPasswordVisibleField;

    @FXML private CheckBox showPasswordsCheckBox;
    @FXML private Button       registerActionButton;

    @FXML
    public void initialize() {
        // Отметить "Регистрация" как активную
        registerButton.getStyleClass().add("active");
        // Навигация на окно входа
        sideLoginButton.setOnMouseClicked(this::openLogin);
        // Обработчик самой регистрации
        registerActionButton.setOnMouseClicked(this::handleRegistration);
        // синхронизируем тексты паролей
        passwordVisibleField.textProperty()
                .bindBidirectional(passwordField.textProperty());
        confirmPasswordVisibleField.textProperty()
                .bindBidirectional(confirmPasswordField.textProperty());

        // переключаем видимость/managed в зависимости от чекбокса
        passwordVisibleField.visibleProperty()
                .bind(showPasswordsCheckBox.selectedProperty());
        passwordVisibleField.managedProperty()
                .bind(showPasswordsCheckBox.selectedProperty());

        confirmPasswordVisibleField.visibleProperty()
                .bind(showPasswordsCheckBox.selectedProperty());
        confirmPasswordVisibleField.managedProperty()
                .bind(showPasswordsCheckBox.selectedProperty());

        passwordField.visibleProperty()
                .bind(showPasswordsCheckBox.selectedProperty().not());
        passwordField.managedProperty()
                .bind(showPasswordsCheckBox.selectedProperty().not());

        confirmPasswordField.visibleProperty()
                .bind(showPasswordsCheckBox.selectedProperty().not());
        confirmPasswordField.managedProperty()
                .bind(showPasswordsCheckBox.selectedProperty().not());
    }

    private void openLogin(MouseEvent event) {
        try {
            // Сброс активных стилей
            registerButton.getStyleClass().remove("active");
            sideLoginButton.getStyleClass().add("active");

            Stage stage = (Stage) sideLoginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/org/db/buildingfirm/FXML/Login.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Войти");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRegistration(MouseEvent event) {
        String login = loginField.getText().trim();
        String pwd   = passwordField.getText();
        String conf  = confirmPasswordField.getText();

        if (login.isEmpty() || pwd.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Логин и пароль не могут быть пустыми").showAndWait();
            return;
        }

        if (!pwd.equals(conf)) {
            new Alert(Alert.AlertType.ERROR, "Пароли не совпадают").showAndWait();
            return;
        }

        // 1) Проверяем, нет ли уже такого логина
        String checkSql = "SELECT COUNT(*) FROM Пользователь WHERE Логин = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, login);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                new Alert(Alert.AlertType.ERROR, "Пользователь с таким логином уже существует").showAndWait();
                return;
            }

            // 2) Вставляем нового пользователя с ролью Client (ID_роли = 3)
            String insertSql = "INSERT INTO Пользователь (Логин, Пароль, ID_роли) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, login);
                insertStmt.setString(2, pwd);
                insertStmt.setInt(3, 3);  // 3 — это ID роли «Client»
                insertStmt.executeUpdate();
            }

            new Alert(Alert.AlertType.INFORMATION, "Регистрация прошла успешно!").showAndWait();

            // 3) После успешной регистрации возвращаемся на экран логина
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/org/db/buildingfirm/FXML/Login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) registerActionButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Авторизация");
            stage.show();

        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка при регистрации: " + ex.getMessage())
                    .showAndWait();
        } catch (Exception io) {
            io.printStackTrace();
        }
    }
}
