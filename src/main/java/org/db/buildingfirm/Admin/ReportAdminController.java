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
    @FXML private CheckBox distinct;

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
    @FXML private Button exportButton;

    public void initialize() {
        distinct.setOnAction(e -> {
            if (buildingComboBox.getValue() != null) {
                loadMaterials(buildingComboBox.getValue());
            }
        });

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

        String baseQuery = distinct.isSelected()
                ? "SELECT m.Наименование, GROUP_CONCAT(DISTINCT p.Наименование SEPARATOR ', ') AS Поставщики, " +
                "SUM(rm.Количество) AS Количество, SUM(rm.Стоимость) AS Стоимость " +
                "FROM Расход_материалов rm " +
                "JOIN Материал m ON rm.ID_материала = m.ID_материала " +
                "JOIN Поставщик p ON rm.ID_поставщика = p.ID_поставщика " +
                "JOIN Здание з ON rm.ID_здания = з.ID_здания " +
                "WHERE з.Наименование = ? " +
                "GROUP BY m.Наименование"
                : "SELECT m.Наименование, p.Наименование, rm.Количество, rm.Стоимость " +
                "FROM Расход_материалов rm " +
                "JOIN Материал m ON rm.ID_материала = m.ID_материала " +
                "JOIN Поставщик p ON rm.ID_поставщика = p.ID_поставщика " +
                "JOIN Здание з ON rm.ID_здания = з.ID_здания " +
                "WHERE з.Наименование = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(baseQuery)) {

            pst.setString(1, building);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    data.add(new MaterialUsage(
                            rs.getString(1),      // Материал
                            rs.getString(2),      // Поставщик или поставщики
                            rs.getInt(3),         // Количество
                            rs.getDouble(4)       // Стоимость
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        materialTable.setItems(data);
    }


    @FXML
    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет в Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Отчет по зданию");
                sheet.getPrintSetup().setLandscape(true); // Горизонтальная ориентация
                sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);

                sheet.getPrintSetup().setFitWidth((short) 1);
                sheet.setFitToPage(true);

                int rowNum = 0;

                rowNum = writeSection(sheet, "График работ", scheduleTable, rowNum, workbook);
                rowNum = writeSection(sheet, "Состав бригад", crewTable, rowNum + 2, workbook);
                writeSection(sheet, "Используемые материалы", materialTable, rowNum + 2, workbook);

                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private <T> int writeSection(Sheet sheet, String sectionTitle, TableView<T> table, int startRow, Workbook workbook) {
        // Стили
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);

        CellStyle tableHeaderStyle = workbook.createCellStyle();
        tableHeaderStyle.cloneStyleFrom(headerStyle);
        tableHeaderStyle.setFont(workbook.createFont());
        Font tableFont = workbook.createFont();
        tableFont.setBold(true);
        tableHeaderStyle.setFont(tableFont);


        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        // Заголовок раздела
        Row titleRow = sheet.createRow(startRow);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(sectionTitle);
        titleCell.setCellStyle(headerStyle);

        // Заголовки таблицы
        Row headerRow = sheet.createRow(startRow + 1);
        for (int i = 0; i < table.getColumns().size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(table.getColumns().get(i).getText());
            cell.setCellStyle(tableHeaderStyle);
        }

        // Данные таблицы
        for (int i = 0; i < table.getItems().size(); i++) {
            Row row = sheet.createRow(startRow + 2 + i);
            T item = table.getItems().get(i);
            for (int j = 0; j < table.getColumns().size(); j++) {
                Object value = table.getColumns().get(j).getCellData(item);
                Cell cell = row.createCell(j);
                cell.setCellValue(value != null ? value.toString() : "");
                cell.setCellStyle(cellStyle);
            }
        }

        for (int i = 0; i < table.getColumns().size(); i++) {
            sheet.autoSizeColumn(i);
        }

        return startRow + 2 + table.getItems().size();
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