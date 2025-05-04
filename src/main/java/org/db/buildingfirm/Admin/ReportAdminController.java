package org.db.buildingfirm.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.fxml.FXMLLoader;
import org.apache.poi.ss.usermodel.Cell;
import org.db.buildingfirm.DatabaseConnection;
import org.db.buildingfirm.DTO.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;

public class ReportAdminController {

    @FXML private ComboBox<String> buildingComboBox;

    @FXML
    private Button exportButton;

    @FXML private TableView<WorkSchedule> scheduleTable;
    @FXML private TableColumn<WorkSchedule, String> startDateColumn;
    @FXML private TableColumn<WorkSchedule, String> endDateColumn;
    @FXML private TableColumn<WorkSchedule, String> crewNameColumn;

    @FXML private TableView<CrewAssignment> crewTable;
    @FXML private TableColumn<CrewAssignment, String> lastNameColumn;
    @FXML private TableColumn<CrewAssignment, String> firstNameColumn;
    @FXML private TableColumn<CrewAssignment, String> patronymicColumn;
    @FXML private TableColumn<CrewAssignment, String> crewColumn;

    @FXML private TableView<MaterialUsage> materialTable;
    @FXML private TableColumn<MaterialUsage, String> materialNameColumn;
    @FXML private TableColumn<MaterialUsage, String> supplierColumn;
    @FXML private TableColumn<MaterialUsage, Integer> quantityColumn;
    @FXML private TableColumn<MaterialUsage, Double> costColumn;

    @FXML private Button backButton;

    public void initialize() {
        initColumns();
        loadBuildings();
    }

    private void initColumns() {
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        crewNameColumn.setCellValueFactory(new PropertyValueFactory<>("crewName"));

        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        patronymicColumn.setCellValueFactory(new PropertyValueFactory<>("patronymic"));
        crewColumn.setCellValueFactory(new PropertyValueFactory<>("crewName"));

        materialNameColumn.setCellValueFactory(new PropertyValueFactory<>("materialName"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
    }

    private void loadBuildings() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT Наименование FROM Здание");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                buildingComboBox.getItems().add(rs.getString("Наименование"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBuildingSelected() {
        String building = buildingComboBox.getValue();
        if (building == null) return;
        loadSchedule(building);
        loadCrew(building);
        loadMaterials(building);
    }

    private void loadSchedule(String building) {
        ObservableList<WorkSchedule> data = FXCollections.observableArrayList();
        String sql = "SELECT gs.Дата_начала_строительства, gs.Дата_окончания_строительства, б.Наименование " +
                "FROM График_работ gs " +
                "JOIN Бригада б ON gs.ID_бригады = б.ID_бригады " +
                "JOIN Здание з ON gs.ID_здания = з.ID_здания " +
                "WHERE з.Наименование = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, building);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    data.add(new WorkSchedule(
                            rs.getString(1), rs.getString(2), rs.getString(3)
                    ));
                }
                scheduleTable.setItems(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCrew(String building) {
        ObservableList<CrewAssignment> data = FXCollections.observableArrayList();
        String sql = "SELECT р.Фамилия, р.Имя, р.Отчество, б.Наименование FROM Состав_бригады с " +
                "JOIN Рабочий р ON с.ID_рабочего = р.ID_рабочего " +
                "JOIN Бригада б ON с.ID_бригады = б.ID_бригады " +
                "JOIN График_работ г ON г.ID_бригады = б.ID_бригады " +
                "JOIN Здание з ON г.ID_здания = з.ID_здания " +
                "WHERE з.Наименование = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, building);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    data.add(new CrewAssignment(
                            rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)
                    ));
                }
                crewTable.setItems(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMaterials(String building) {
        ObservableList<MaterialUsage> data = FXCollections.observableArrayList();
        String sql = "SELECT m.Наименование, p.Наименование, rm.Количество, rm.Стоимость " +
                "FROM Расход_материалов rm " +
                "JOIN Материал m ON rm.ID_материала = m.ID_материала " +
                "JOIN Поставщик p ON rm.ID_поставщика = p.ID_поставщика " +
                "JOIN Здание з ON rm.ID_здания = з.ID_здания " +
                "WHERE з.Наименование = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, building);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    data.add(new MaterialUsage(
                            rs.getString(1), rs.getString(2), rs.getInt(3), rs.getDouble(4)
                    ));
                }
                materialTable.setItems(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет в Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet scheduleSheet = workbook.createSheet("График работ");
                Sheet crewSheet = workbook.createSheet("Состав бригад");
                Sheet materialsSheet = workbook.createSheet("Материалы");

                createSheet(scheduleSheet, scheduleTable);
                createSheet(crewSheet, crewTable);
                createSheet(materialsSheet, materialTable);

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private <T> void createSheet(Sheet sheet, TableView<T> table) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < table.getColumns().size(); i++) {
            TableColumn<T, ?> col = table.getColumns().get(i);
            Cell cell = header.createCell(i);
            cell.setCellValue(col.getText());
        }

        for (int i = 0; i < table.getItems().size(); i++) {
            Row row = sheet.createRow(i + 1);
            T item = table.getItems().get(i);
            for (int j = 0; j < table.getColumns().size(); j++) {
                Object cellData = table.getColumns().get(j).getCellData(item);
                row.createCell(j).setCellValue(cellData != null ? cellData.toString() : "");
            }
        }

        for (int i = 0; i < table.getColumns().size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    @FXML
    private void goBack() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/db/buildingfirm/FXML/Admin.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Администратор");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
