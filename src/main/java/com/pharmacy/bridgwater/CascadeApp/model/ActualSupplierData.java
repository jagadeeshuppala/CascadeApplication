package com.pharmacy.bridgwater.CascadeApp.model;

import lombok.*;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ActualSupplierData {
    private String supplier;
    private String description;
    private Double cascadePrice;
    private Double supplierPrice;
    private String code;
    private String cascadeStatus;
    private String supplierStatus;

    private Integer quantity;
    private Double tariff;
    private Double tariffAfterDeduction;
    private Double concession;

    private Double definitePrice;
    private String definiteStatus;

    /*@Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ActualSupplierData that = (ActualSupplierData) o;
        return Objects.equals(description, that.description) && Objects.equals(cascadePrice, that.cascadePrice) && Objects.equals(cascadeStatus, that.cascadeStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, cascadePrice, cascadeStatus);
    }*/

    @Override
    public String toString() {
        return "ActualSupplierData{" +
                "supplier='" + supplier + '\'' +
                ", description='" + description + '\'' +
                ", cascadePrice=" + cascadePrice +
                ", supplierPrice=" + supplierPrice +
                ", code='" + code + '\'' +
                ", cascadeStatus='" + cascadeStatus + '\'' +
                ", supplierStatus='" + supplierStatus + '\'' +
                ", quantity=" + quantity +
                ", tariff=" + tariff +
                ", tariffAfterDeduction=" + tariffAfterDeduction +
                ", concession=" + concession +
                ", definitePrice=" + definitePrice +
                ", definiteStatus='" + definiteStatus + '\'' +
                '}';
    }
}
