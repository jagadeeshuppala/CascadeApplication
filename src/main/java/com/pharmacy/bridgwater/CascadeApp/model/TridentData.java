package com.pharmacy.bridgwater.CascadeApp.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TridentData {
    private String description;
    private Double cascadePrice;
    private Double tridentPrice;
    private String code;
    private String cascadeStatus;
    private String tridentStatus;


}
