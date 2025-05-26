package org.db.buildingfirm.DTO;

import javafx.beans.property.*;

public class Crew {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty foreman;

    public Crew(int id, String name, String foreman) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.foreman = new SimpleStringProperty(foreman);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getForeman() {
        return foreman.get();
    }

    public StringProperty foremanProperty() {
        return foreman;
    }
}
