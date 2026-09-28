package com.safeway.tech.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "addresses")
@Getter @Setter
@NoArgsConstructor
public class Address extends BaseEntity {

    @Column(nullable = false)
    private String street;

    @Column(nullable = false, length = 10)
    private String number;

    @Column(name = "additional_details", length = 100)
    private String additionalDetails;

    @Column(nullable = false, length = 100)
    private String neighborhood;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "federal_unit", nullable = false, length = 2)
    private String federalUnit;

    @Column(name = "zip_code", nullable = false, length = 9)
    private String zipCode;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private Boolean principal = false;
}
