package org.db.buildingfirm.DTO;

import javafx.beans.property.*;

public class SupplyRecord {
    private final IntegerProperty id      = new SimpleIntegerProperty();
    private final StringProperty  name    = new SimpleStringProperty();
    private final StringProperty  type    = new SimpleStringProperty();
    private final IntegerProperty qty     = new SimpleIntegerProperty();
    private final StringProperty  date    = new SimpleStringProperty();

    public SupplyRecord(int id, String name, String type, int qty, String date) {
        this.id.set(id);
        this.name.set(name);
        this.type.set(type);
        this.qty.set(qty);
        this.date.set(date);
    }

    public IntegerProperty idProperty()   { return id; }
    public StringProperty  nameProperty() { return name; }
    public StringProperty  typeProperty() { return type; }
    public IntegerProperty qtyProperty()  { return qty; }
    public StringProperty  dateProperty() { return date; }

    public int    getId()   { return id.get(); }
    public String getName() { return name.get(); }
    public String getType() { return type.get(); }
    public int    getQty()  { return qty.get(); }
    public String getDate() { return date.get(); }
}
