package com.pharmacy.bridgwater.CascadeApp.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CascadeSupplier {
    private String supplier;
    private Double price;
    private String code;
    private String status;
    private Integer quantity;
    private Double tariff;
    private Double tariffAfterDeduction;
    private Double concession;


}
