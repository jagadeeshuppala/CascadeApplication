package com.pharmacy.bridgwater.CascadeApp.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BestwayData {
    private String description;
    private Double cascadePrice;
    private Double bestwayPrice;
    private String code;
    private String cascadeStatus;
    private String bestwayStatus;


}
