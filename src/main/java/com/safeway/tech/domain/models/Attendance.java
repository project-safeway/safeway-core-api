package com.safeway.tech.domain.models;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attendances")
@Getter @Setter
@NoArgsConstructor
public class Attendance extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatusEnum status;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
}
