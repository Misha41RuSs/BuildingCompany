package org.db.buildingfirm.DTO;

public class MaterialUsage {
    private final String materialName;
    private final String supplierName;
    private final int quantity;
    private final double totalCost;

    public MaterialUsage(String materialName, String supplierName, int quantity, double totalCost) {
        this.materialName = materialName;
        this.supplierName = supplierName;
        this.quantity = quantity;
        this.totalCost = totalCost;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalCost() {
        return totalCost;
    }
}
