package com.pharmacy.bridgwater.CascadeApp.model;

import lombok.*;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SigmaData {
    private String description;
    private Double price;
    private String code;
    private String status;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SigmaData sigmaData = (SigmaData) o;
        return Objects.equals(description, sigmaData.description) && Objects.equals(price, sigmaData.price) && Objects.equals(status, sigmaData.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, price, status);
    }
}
