package org.db.buildingfirm.DTO;

import javafx.beans.property.*;

public class Worker {
    private final IntegerProperty id;
    private final StringProperty fullName;
    private final IntegerProperty experience;
    private final StringProperty phone;
    private final StringProperty address;
    private final StringProperty specialty;

    public Worker(int id, String fullName, int experience, String phone, String address, String specialty) {
        this.id = new SimpleIntegerProperty(id);
        this.fullName = new SimpleStringProperty(fullName);
        this.experience = new SimpleIntegerProperty(experience);
        this.phone = new SimpleStringProperty(phone);
        this.address = new SimpleStringProperty(address);
        this.specialty = new SimpleStringProperty(specialty);
    }
    public Worker(int id, String fullName) {
        this.id = new SimpleIntegerProperty(id);
        this.fullName = new SimpleStringProperty(fullName);
        this.experience = new SimpleIntegerProperty(0);
        this.phone = new SimpleStringProperty("");
        this.address = new SimpleStringProperty("");
        this.specialty = new SimpleStringProperty("");
    }


    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getFullName() {
        return fullName.get();
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public int getExperience() {
        return experience.get();
    }

    public IntegerProperty experienceProperty() {
        return experience;
    }

    public String getPhone() {
        return phone.get();
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getAddress() {
        return address.get();
    }

    public StringProperty addressProperty() {
        return address;
    }

    public String getSpecialty() {
        return specialty.get();
    }

    public StringProperty specialtyProperty() {
        return specialty;
    }
}
