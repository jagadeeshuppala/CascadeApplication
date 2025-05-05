package com.pharmacy.bridgwater.CascadeApp.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BnsData {
    private String description;
    private Double cascadePrice;
    private Double bnsPrice;
    private String code;
    private String cascadeStatus;
    private String bnsStatus;


}
