package com.safeway.tech.domain.models;

import com.safeway.tech.domain.enums.RouteTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;

@Entity
@Table(name = "routes")
@Getter @Setter
@NoArgsConstructor
public class Route extends BaseEntity {

    @Column(length = 150)
    private String name;

    @Column(name = "start_time")
    private Time startTime;

    @Column(name = "end_time")
    private Time endTime;

    @Column(name = "route_type", nullable = false)
    private RouteTypeEnum routeType;

    @ManyToOne
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport;
}
