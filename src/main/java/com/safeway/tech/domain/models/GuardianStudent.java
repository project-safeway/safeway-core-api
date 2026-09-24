package com.safeway.tech.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "guardian_students",
        uniqueConstraints = @UniqueConstraint(columnNames = {"guardian_id", "student_id"}, name = "uk_guardian_students")
)
@Getter @Setter
@NoArgsConstructor
public class GuardianStudent extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

}
