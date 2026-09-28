package com.safeway.tech.domain.models;

import com.safeway.tech.domain.enums.InvitationStatusEnum;
import com.safeway.tech.domain.enums.InvitationTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "guardian_invitations",
        uniqueConstraints = @UniqueConstraint(columnNames = "token", name = "uk_guardian_invitations_token")
)
@Getter @Setter
@NoArgsConstructor
public class GuardianInvitation extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationTypeEnum type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatusEnum status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @ManyToOne
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "target_email")
    private String targetEmail;

    @Column(name = "target_phone_number", length = 15)
    private String targetPhoneNumber;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;
}
