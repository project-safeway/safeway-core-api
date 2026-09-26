package com.safeway.tech.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter @Setter
@NoArgsConstructor
public class Student extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String professor;

    private LocalDate birthdate;

    private Integer grade;

    @Column(length = 20)
    private String classroom;

    @Column(name = "monthly_fee", nullable = false)
    private Double monthlyFee;

    @Min(1)
    @Max(31)
    @Column(name = "due_date", nullable = false)
    private Integer dueDate;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne
    @JoinColumn(name = "transport_id")
    private Transport transport;
}
