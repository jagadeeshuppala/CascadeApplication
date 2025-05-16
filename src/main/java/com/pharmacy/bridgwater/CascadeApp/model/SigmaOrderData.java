package com.pharmacy.bridgwater.CascadeApp.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SigmaOrderData {
    private String pip;
    private Integer quantity;


}
