package org.db.buildingfirm.Utilities;

public class CrewAssignment {
    private final String lastName;
    private final String firstName;
    private final String patronymic;
    private final String crewName;

    public CrewAssignment(String lastName, String firstName, String patronymic, String crewName) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.crewName = crewName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public String getCrewName() {
        return crewName;
    }
}
