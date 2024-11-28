package orgaplan.beratung.kreditunterlagen.model;

import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;
import orgaplan.beratung.kreditunterlagen.Types.UserRole;

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
    @Column(name = "vermittler_id", length = 36, nullable = false)
    private String vermittlerId;

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

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "with_second_partner")
    private Boolean withSecondPartner;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @Column(name = "document_upload_percentage", precision = 5, scale = 1, nullable = false)
    private BigDecimal documentUploadPercentage;

    @NotNull
    @Column(name = "forwarded_banks", nullable = false)
    private Boolean forwardedBanks;

    @Column(name = "forwarded_banks_at")
    private LocalDateTime forwardedBanksAt;

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