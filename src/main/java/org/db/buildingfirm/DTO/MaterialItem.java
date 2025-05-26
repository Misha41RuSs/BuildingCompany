package org.db.buildingfirm.DTO;

import javafx.beans.property.*;

public class MaterialItem {
    private final IntegerProperty id    = new SimpleIntegerProperty();
    private final StringProperty  name  = new SimpleStringProperty();
    private final StringProperty  unit  = new SimpleStringProperty();
    private final IntegerProperty stock = new SimpleIntegerProperty();

    public MaterialItem(int id, String name, String unit, int stock) {
        this.id.set(id);
        this.name.set(name);
        this.unit.set(unit);
        this.stock.set(stock);
    }

    public IntegerProperty idProperty()    { return id; }
    public StringProperty  nameProperty()  { return name; }
    public StringProperty  unitProperty()  { return unit; }
    public IntegerProperty stockProperty() { return stock; }

    public int    getId()    { return id.get(); }
    public String getName()  { return name.get(); }
    public String getUnit()  { return unit.get(); }
    public int    getStock() { return stock.get(); }
}
