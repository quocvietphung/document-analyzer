package orgaplan.beratung.kreditunterlagen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import orgaplan.beratung.kreditunterlagen.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotNull
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotNull
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotNull
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @NotNull
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotNull
    @Column(name = "password", nullable = false, unique = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "role", nullable = false)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_admin_id")
    private User assignedByAdmin;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @Column(name = "document_upload_percentage", precision = 5, scale = 1, nullable = false)
    private BigDecimal documentUploadPercentage;

    @NotNull
    @Column(name = "terms_and_conditions_accepted", nullable = false)
    private Boolean termsAndConditionsAccepted;

    @NotNull
    @Column(name = "privacy_policy_accepted", nullable = false)
    private Boolean privacyPolicyAccepted;

    @NotNull
    @Column(name = "usage_terms_accepted", nullable = false)
    private Boolean usageTermsAccepted;

    @NotNull
    @Column(name = "consent_terms_accepted", nullable = false)
    private Boolean consentTermsAccepted;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}