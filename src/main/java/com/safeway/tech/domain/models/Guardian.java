package com.safeway.tech.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "guardians")
@Getter @Setter
@NoArgsConstructor
public class Guardian extends BaseEntity {

    @Column(nullable = false, length = 45)
    private String name;

    @Column(name = "primary_phone_number", nullable = false, length = 15)
    private String primaryPhoneNumber;

    @Column(name = "secondary_phone_number", length = 15)
    private String secondaryPhoneNumber;

    private String email;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
}
