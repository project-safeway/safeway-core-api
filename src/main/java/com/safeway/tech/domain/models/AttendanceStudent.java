package com.safeway.tech.domain.models;

import com.safeway.tech.domain.enums.PresenceStatusEnum;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_students")
@Getter @Setter
@NoArgsConstructor
public class AttendanceStudent extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PresenceStatusEnum presence;

    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}
