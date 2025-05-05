package com.pharmacy.bridgwater.CascadeApp.model;

import lombok.*;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AahData {
    private String description;
    private Double cascadePrice;
    private Double aahPrice;
    private String code;
    private String cascadeStatus;
    private String aahStatus;


}
