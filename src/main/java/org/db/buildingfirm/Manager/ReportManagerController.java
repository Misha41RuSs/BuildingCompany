package org.db.buildingfirm.Manager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.db.buildingfirm.DatabaseConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportManagerController {

    @FXML
    private ComboBox<String> materialComboBox;

    @FXML
    private CheckBox priceCheckBox;

    @FXML
    private CheckBox groupProvider;

    @FXML
    private CheckBox totalShow;

    @FXML
    private TableView<MaterialReport> reportTable;

    @FXML
    private TableColumn<MaterialReport, String> materialColumn;

    @FXML
    private TableColumn<MaterialReport, String> supplierColumn;

    @FXML
    private TableColumn<MaterialReport, Integer> quantityColumn;

    @FXML
    private TableColumn<MaterialReport, Double> totalCostColumn;

    @FXML
    private Button backButton;

    @FXML
    private Button excelButton;

    @FXML
    private Button generateReportButton;

    private ObservableList<MaterialReport> materialReports = FXCollections.observableArrayList();

    @FXML
    void backToManager(MouseEvent event) {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/Manager.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Менеджерская форма");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<String> loadMaterialsFromDatabase() {
        ObservableList<String> materials = FXCollections.observableArrayList();
        String query = "SELECT DISTINCT Наименование FROM Материал"; // Используем DISTINCT для уникальных материалов
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                materials.add(rs.getString("Наименование"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materials;
    }


    @FXML
    void generateReport(MouseEvent event) {
        materialReports.clear();

        String selectedMaterial = materialComboBox.getValue();
        if (selectedMaterial == null || selectedMaterial.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Не выбран материал", "Пожалуйста, выберите материал.");
            return;
        }

        boolean groupBy = groupProvider.isSelected();

        String query;
        if (groupBy) {
            query = "SELECT m.Наименование, p.Наименование AS Поставщик, " +
                    "SUM(rm.Количество) AS Количество, SUM(rm.Стоимость) AS Стоимость " +
                    "FROM Расход_материалов rm " +
                    "JOIN Материал m ON rm.ID_материала = m.ID_материала " +
                    "JOIN Поставщик p ON rm.ID_поставщика = p.ID_поставщика " +
                    "WHERE m.Наименование = ? " +
                    "GROUP BY m.Наименование, p.Наименование";
        } else {
            query = "SELECT m.Наименование, p.Наименование AS Поставщик, rm.Количество, rm.Стоимость " +
                    "FROM Расход_материалов rm " +
                    "JOIN Материал m ON rm.ID_материала = m.ID_материала " +
                    "JOIN Поставщик p ON rm.ID_поставщика = p.ID_поставщика " +
                    "WHERE m.Наименование = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setString(1, selectedMaterial);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String material = rs.getString("Наименование");
                    String supplier = rs.getString("Поставщик");
                    int quantity = rs.getInt("Количество");
                    double totalCost = rs.getDouble("Стоимость");

                    materialReports.add(new MaterialReport(material, supplier, quantity, totalCost));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка запроса", e.getMessage());
        }

        reportTable.setItems(materialReports);
    }


    @FXML
    void exportExcel(MouseEvent event) {
        if (materialReports.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Нет данных", "Сначала сгенерируйте отчет.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel файлы (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("material_report.xlsx");

        Stage stage = (Stage) reportTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Отчет");

        // Стили
        CellStyle boldHeaderStyle = workbook.createCellStyle();
        boldHeaderStyle.setBorderTop(BorderStyle.THIN);
        boldHeaderStyle.setBorderBottom(BorderStyle.THIN);
        boldHeaderStyle.setBorderLeft(BorderStyle.THIN);
        boldHeaderStyle.setBorderRight(BorderStyle.THIN);
        boldHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldHeaderStyle.setFont(boldFont);

        CellStyle regularStyle = workbook.createCellStyle();
        regularStyle.setBorderTop(BorderStyle.THIN);
        regularStyle.setBorderBottom(BorderStyle.THIN);
        regularStyle.setBorderLeft(BorderStyle.THIN);
        regularStyle.setBorderRight(BorderStyle.THIN);
        regularStyle.setAlignment(HorizontalAlignment.CENTER);

        // Заголовки
        String[] columns = {"Материал", "Поставщик", "Количество", "Общая стоимость"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(boldHeaderStyle);
        }

        // Данные
        int rowNum = 1;
        int totalQuantity = 0;
        double totalCost = 0;

        for (MaterialReport report : materialReports) {
            Row row = sheet.createRow(rowNum++);

            Cell materialCell = row.createCell(0);
            materialCell.setCellValue(report.getMaterial());
            materialCell.setCellStyle(regularStyle);

            Cell supplierCell = row.createCell(1);
            supplierCell.setCellValue(report.getSupplier());
            supplierCell.setCellStyle(regularStyle);

            Cell quantityCell = row.createCell(2);
            quantityCell.setCellValue(report.getQuantity());
            quantityCell.setCellStyle(regularStyle);

            Cell costCell = row.createCell(3);
            costCell.setCellValue(report.getTotalCost());
            costCell.setCellStyle(regularStyle);

            // подсчет итогов
            totalQuantity += report.getQuantity();
            totalCost += report.getTotalCost();
        }

        // Добавим строку "Итого", если выбран чекбокс
        if (totalShow.isSelected()) {
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итого:");
            totalLabelCell.setCellStyle(boldHeaderStyle);

            Cell emptyCell = totalRow.createCell(1);
            emptyCell.setCellStyle(boldHeaderStyle);

            Cell totalQtyCell = totalRow.createCell(2);
            totalQtyCell.setCellValue(totalQuantity);
            totalQtyCell.setCellStyle(boldHeaderStyle);

            Cell totalCostCell = totalRow.createCell(3);
            totalCostCell.setCellValue(totalCost);
            totalCostCell.setCellStyle(boldHeaderStyle);
        }

        // Автоширина
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Сохраняем
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Отчет сохранён:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка сохранения", e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }





    public static class MaterialReport {
        private SimpleStringProperty material;
        private SimpleStringProperty supplier;
        private SimpleIntegerProperty quantity;
        private SimpleDoubleProperty totalCost;

        public MaterialReport(String material, String supplier, int quantity, double totalCost) {
            this.material = new SimpleStringProperty(material);
            this.supplier = new SimpleStringProperty(supplier);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.totalCost = new SimpleDoubleProperty(totalCost);
        }

        public String getMaterial() {
            return material.get();
        }

        public String getSupplier() {
            return supplier.get();
        }

        public int getQuantity() {
            return quantity.get();
        }

        public double getTotalCost() {
            return totalCost.get();
        }

        public SimpleStringProperty materialProperty() {
            return material;
        }

        public SimpleStringProperty supplierProperty() {
            return supplier;
        }

        public SimpleIntegerProperty quantityProperty() {
            return quantity;
        }

        public SimpleDoubleProperty totalCostProperty() {
            return totalCost;
        }
    }

    public void initialize() {
        // Загружаем материалы в ComboBox
        materialComboBox.setItems(loadMaterialsFromDatabase());

        // Инициализация колонок таблицы
        materialColumn.setCellValueFactory(cellData -> cellData.getValue().materialProperty());
        supplierColumn.setCellValueFactory(cellData -> cellData.getValue().supplierProperty());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        totalCostColumn.setCellValueFactory(cellData -> cellData.getValue().totalCostProperty().asObject());
    }

}
