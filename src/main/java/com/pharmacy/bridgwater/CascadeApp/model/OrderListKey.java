package com.pharmacy.bridgwater.CascadeApp.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderListKey {
    private Integer sno;
    private String orderListPipCode;
    private String orderListDesc;
    private String bnsPipCode;
    private Double bnsPhonePrice;

}
