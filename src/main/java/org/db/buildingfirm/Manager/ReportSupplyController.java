package org.db.buildingfirm.Manager;

import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.db.buildingfirm.DatabaseConnection;
// в начале файла
import org.db.buildingfirm.Manager.MaterialItem;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ReportSupplyController {

    @FXML private TableView<SupplyRecord> table;
    @FXML private TableColumn<SupplyRecord,Integer> colId;
    @FXML private TableColumn<SupplyRecord,String>  colName;
    @FXML private TableColumn<SupplyRecord,String>  colType;
    @FXML private TableColumn<SupplyRecord,Integer> colQty;
    @FXML private TableColumn<SupplyRecord,String>  colDate;

    @FXML private DatePicker dpFrom, dpTo;
    @FXML private CheckBox cbGroup;
    @FXML private Button btnFilter, btnExport, btnAdd, backButton;

    private final ObservableList<SupplyRecord> data = FXCollections.observableArrayList();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        colId  .setCellValueFactory(c -> c.getValue().idProperty().asObject());
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colType.setCellValueFactory(c -> c.getValue().typeProperty());
        colQty .setCellValueFactory(c -> c.getValue().qtyProperty().asObject());
        colDate.setCellValueFactory(c -> c.getValue().dateProperty());

        table.setItems(data);

        // по умолчанию — последние 30 дней
        dpTo.setValue(LocalDate.now());
        dpFrom.setValue(LocalDate.now().minusDays(30));

        applyFilter(null);
    }

    /** Возврат на форму менеджера */
    @FXML
    void goBack(ActionEvent ev)throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/org/db/buildingfirm/FXML/Manager.fxml"));
        Parent root = loader.load();
        Stage st = (Stage)((Node)ev.getSource()).getScene().getWindow();
        st.setScene(new Scene(root));
        st.setTitle("Менеджер");
        st.show();
    }

    /** Применить фильтр / группировку */
    @FXML
    void applyFilter(ActionEvent ev) {
        btnExport.setDisable(data.isEmpty());

        data.clear();
        boolean group = cbGroup.isSelected();

        LocalDate from = dpFrom.getValue();
        LocalDate to   = dpTo.getValue();

        // Основной подзапрос: объединение заказов и поставок
        String base = """
        SELECT ID_заказа_поставщику AS id,
               m.Наименование AS name,
               'Заказ' AS type,
               zp.Количество AS qty,
               zp.Дата_создания AS dt
        FROM Заказ_поставщику zp
        JOIN Материал m ON zp.ID_материала = m.ID_материала
        WHERE zp.Дата_создания BETWEEN ? AND ?
        UNION ALL
        SELECT pm.ID_поставки AS id,
               m.Наименование AS name,
               'Поставка' AS type,
               pm.Количество AS qty,
               pm.Дата AS dt
        FROM Поставка_материала pm
        JOIN Материал m ON pm.ID_материала = m.ID_материала
        WHERE pm.Дата BETWEEN ? AND ?
    """;

        // Финальный SQL: группировка или нет
        String sql;
        if (group) {
            sql = "SELECT 0 AS id, " +
                    "       name, " +
                    "       'Итого' AS type, " +
                    "       SUM(qty) AS qty, " +
                    "       '' AS dt " +
                    "FROM (" + base + ") AS t " +
                    "GROUP BY name " +
                    "HAVING SUM(qty) <> 0 " +
                    "ORDER BY qty DESC";
        }
        else {
            sql = base + " ORDER BY dt DESC";
        }

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(to.atTime(23, 59, 59)));
            ps.setTimestamp(3, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(4, Timestamp.valueOf(to.atTime(23, 59, 59)));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new SupplyRecord(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("qty"),
                        rs.getString("dt") == null ? "" : rs.getString("dt")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Ошибка при загрузке отчета:\n" + ex.getMessage())
                    .showAndWait();
        }
    }



    /** Показывает диалог добавления новой поступления */
    @FXML
    void showAddDialog(ActionEvent ev) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Добавить поступление");

        // поля: ComboBox материалов, TextField кол-ва, DatePicker
        ComboBox<MaterialItem> cbMat = new ComboBox<>();
        TextField tfQty = new TextField();
        DatePicker dp = new DatePicker(LocalDate.now());

        // загрузим материалы
        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement("SELECT ID_материала, Наименование FROM Материал");
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                cbMat.getItems().add(new MaterialItem(
                        rs.getInt(1), rs.getString(2), "", 0));
            }
        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR, "Не удалось загрузить список материалов")
                    .showAndWait();
        }

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10);
        g.add(new Label("Материал:"), 0,0);
        g.add(cbMat,               1,0);
        g.add(new Label("Кол-во:"),  0,1);
        g.add(tfQty,               1,1);
        g.add(new Label("Дата:"),    0,2);
        g.add(dp,                   1,2);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> r = dlg.showAndWait();
        if (r.isEmpty() || r.get()!=ButtonType.OK) return;

        MaterialItem mat = cbMat.getValue();
        if (mat==null) {
            new Alert(Alert.AlertType.WARNING, "Выберите материал").showAndWait();
            return;
        }
        int qty;
        try { qty = Integer.parseInt(tfQty.getText()); }
        catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Неверное количество").showAndWait();
            return;
        }

        // INSERT в Поставка_материала и обновление склааа
        String ins = """
            INSERT INTO Поставка_материала
              (ID_материала, Количество, Дата)
            VALUES (?, ?, ?)
        """;
        String upd = """
            INSERT INTO Inventory (ID_материала, Количество_на_складе)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE
              Количество_на_складе = Количество_на_складе + VALUES(Количество_на_складе)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement p1 = conn.prepareStatement(ins);
             PreparedStatement p2 = conn.prepareStatement(upd)) {

            conn.setAutoCommit(false);

            p1.setInt(1, mat.getId());
            p1.setInt(2, qty);
            p1.setDate(3, Date.valueOf(dp.getValue()));
            p1.executeUpdate();

            p2.setInt(1, mat.getId());
            p2.setInt(2, qty);
            p2.executeUpdate();

            conn.commit();
            new Alert(Alert.AlertType.INFORMATION,
                    "Поставка добавлена и склад обновлен")
                    .showAndWait();

            applyFilter(null);  // обновим отчет

        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Ошибка при добавлении:\n" + ex.getMessage())
                    .showAndWait();
        }
    }
    @FXML
    void exportExcel(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel файлы", "*.xlsx"));
        File file = fileChooser.showSaveDialog(table.getScene().getWindow());

        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Отчет по поставкам");
            sheet.getPrintSetup().setLandscape(true);
            sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
            sheet.getPrintSetup().setFitWidth((short) 1);
            sheet.setFitToPage(true);

            int rowNum = 0;

            // Шрифт жирный
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 14);

            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            // Границы ячеек
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);

            // Шапка
            Row r1 = sheet.createRow(rowNum++);
            r1.createCell(0).setCellValue("Сформировал менеджер");

            Row r2 = sheet.createRow(rowNum++);
            r2.createCell(0).setCellValue("Дата формирования: " + LocalDate.now());

            Row r3 = sheet.createRow(rowNum++);
            Cell c = r3.createCell(0);
            c.setCellValue("Отчет по поставкам материалов");
            c.setCellStyle(boldStyle);

            rowNum++;

            // Заголовки таблицы
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < table.getColumns().size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(table.getColumns().get(i).getText());
                cell.setCellStyle(boldStyle);
            }

            // Данные
            for (int i = 0; i < table.getItems().size(); i++) {
                Row row = sheet.createRow(rowNum++);
                SupplyRecord item = table.getItems().get(i);
                for (int j = 0; j < table.getColumns().size(); j++) {
                    Object val = table.getColumns().get(j).getCellData(item);
                    Cell cell = row.createCell(j);
                    cell.setCellValue(val != null ? val.toString() : "");
                    cell.setCellStyle(borderStyle);
                }
            }

            // Подписи
            rowNum += 2;
            Row sig1 = sheet.createRow(rowNum++);
            sig1.createCell(0).setCellValue("Подпись менеджера:");
            sig1.createCell(1).setCellValue("____________________");

            Row sig2 = sheet.createRow(rowNum++);
            sig2.createCell(0).setCellValue("Подпись главы предприятия:");
            sig2.createCell(1).setCellValue("____________________");

            for (int i = 0; i < table.getColumns().size(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }

            new Alert(Alert.AlertType.INFORMATION, "Отчет успешно сохранен!").showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка при сохранении файла:\n" + ex.getMessage()).showAndWait();
        }
    }

}
