package org.db.buildingfirm;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private Button sideLoginButton;
    @FXML private Button registerButton;

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerActionButton;

    @FXML
    public void initialize() {
        // Отметить "Регистрация" как активную
        registerButton.getStyleClass().add("active");
        // Навигация на окно входа
        sideLoginButton.setOnMouseClicked(this::openLogin);
        // Обработчик самой регистрации
        registerActionButton.setOnMouseClicked(this::handleRegistration);
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
        String login = loginField.getText();
        String pwd = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!pwd.equals(confirm)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка регистрации");
            alert.setHeaderText("Пароли не совпадают");
            alert.showAndWait();
            return;
        }

        // Здесь добавить логику сохранения нового пользователя
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setHeaderText("Пользователь зарегистрирован");
        alert.showAndWait();
    }
}
