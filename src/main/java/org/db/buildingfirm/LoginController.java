package org.db.buildingfirm;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.sql.*;

public class LoginController {

    @FXML
    private Button loginButton;

    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;

    @FXML
    void auth(MouseEvent event) {
        String login = loginField.getText();
        String password = passwordField.getText();

        // Проверка логина и пароля через базу данных
        if (authenticateUser(login, password)) {
            String role = getRole(login);
            loadNewScene(role);  // Перенаправляем на соответствующую форму
        } else {
            // Сообщение об ошибке
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка авторизации");
            alert.setHeaderText("Неверный логин или пароль");
            alert.showAndWait();
        }
    }

    private boolean authenticateUser(String login, String password) {
        // Создаём подключение к базе данных
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Пользователь WHERE Логин = ? AND Пароль = ?";
            assert connection != null;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, login);
                statement.setString(2, password);

                ResultSet resultSet = statement.executeQuery();
                return resultSet.next();  // Если пользователь найден
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getRole(String login) {
        // Получаем роль пользователя из базы данных
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT r.Наименование FROM Пользователь p JOIN Роль r ON p.ID_роли = r.ID_роли WHERE p.Логин = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, login);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("Наименование");  // Возвращаем роль (Admin, Manager, Client)
                }
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

            // В зависимости от роли пользователя загружаем соответствующую форму
            if (role != null) {
                switch (role) {
                    case "Admin":
                        fxmlFile = "Admin.fxml";
                        fxmlTitle = "Панель админа";
                        break;
                    case "Manager":
                        fxmlFile = "Manager.fxml";
                        fxmlTitle = "Панель менеджера";
                        break;
                    case "Client":
                        fxmlFile = "Client.fxml";
                        fxmlTitle = "Панель клиента";
                        break;
                    default:
                        // Роль не найдена, показываем ошибку
                        return;
                }

                // Используйте правильный путь для FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/" + fxmlFile));
                Scene scene = new Scene(loader.load());
                stage.setScene(scene);
                stage.setTitle(fxmlTitle);
                stage.show();
            } else {
                // Ошибка при получении роли
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка авторизации");
                alert.setHeaderText("Роль не найдена");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
