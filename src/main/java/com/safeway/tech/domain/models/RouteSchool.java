package com.safeway.tech.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "route_schools")
@Getter @Setter
@NoArgsConstructor
public class RouteSchool extends BaseEntity {

    @Column(name = "stop_order")
    private Integer stopOrder;

    @Column(name = "general_order")
    private Integer generalOrder;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
}
