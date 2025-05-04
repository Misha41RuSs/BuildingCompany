package org.db.buildingfirm.Utilities;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

public class MaterialReport {

    private final StringProperty materialName;
    private final StringProperty supplierName;
    private final IntegerProperty quantity;
    private final DoubleProperty price;

    // Конструктор
    public MaterialReport(String materialName, String supplierName, int quantity, double price) {
        this.materialName = new SimpleStringProperty(materialName);
        this.supplierName = new SimpleStringProperty(supplierName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
    }

    // Getters and setters для свойств
    public String getMaterialName() {
        return materialName.get();
    }

    public void setMaterialName(String materialName) {
        this.materialName.set(materialName);
    }

    public String getSupplierName() {
        return supplierName.get();
    }

    public void setSupplierName(String supplierName) {
        this.supplierName.set(supplierName);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    // Свойства для JavaFX TableView
    public StringProperty materialNameProperty() {
        return materialName;
    }

    public StringProperty supplierNameProperty() {
        return supplierName;
    }

    // Return the IntegerProperty for quantity
    public IntegerProperty quantityProperty() {
        return quantity;
    }


    // Return the DoubleProperty for price
    public DoubleProperty priceProperty() {
        return price;
    }



    @Override
    public String toString() {
        return "MaterialReport{" +
                "materialName='" + materialName.get() + '\'' +
                ", supplierName='" + supplierName.get() + '\'' +
                ", quantity=" + quantity.get() +
                ", price=" + price.get() +
                '}';
    }

    // Вложенный класс для фильтров
    public static class Filter {
        private String materialFilter;
        private String supplierFilter;
        private Integer quantityFilter;

        public Filter(String materialFilter, String supplierFilter, Integer quantityFilter) {
            this.materialFilter = materialFilter;
            this.supplierFilter = supplierFilter;
            this.quantityFilter = quantityFilter;
        }

        public String getMaterialFilter() {
            return materialFilter;
        }

        public void setMaterialFilter(String materialFilter) {
            this.materialFilter = materialFilter;
        }

        public String getSupplierFilter() {
            return supplierFilter;
        }

        public void setSupplierFilter(String supplierFilter) {
            this.supplierFilter = supplierFilter;
        }

        public Integer getQuantityFilter() {
            return quantityFilter;
        }

        public void setQuantityFilter(Integer quantityFilter) {
            this.quantityFilter = quantityFilter;
        }
    }
}
