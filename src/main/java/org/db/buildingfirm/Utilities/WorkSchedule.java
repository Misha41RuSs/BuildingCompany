package org.db.buildingfirm.Utilities;

public class WorkSchedule {
    private final String startDate;
    private final String endDate;
    private final String crewName;

    public WorkSchedule(String startDate, String endDate, String crewName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.crewName = crewName;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getCrewName() {
        return crewName;
    }
}
