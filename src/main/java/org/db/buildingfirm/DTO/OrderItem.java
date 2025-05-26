package org.db.buildingfirm.DTO;

import javafx.beans.property.*;

public class OrderItem {
    private final IntegerProperty id;
    private final StringProperty address;

    public OrderItem(int id, String address) {
        this.id = new SimpleIntegerProperty(id);
        this.address = new SimpleStringProperty(address);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getAddress() {
        return address.get();
    }

    public StringProperty addressProperty() {
        return address;
    }
}
