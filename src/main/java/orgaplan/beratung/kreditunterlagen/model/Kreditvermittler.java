package orgaplan.beratung.kreditunterlagen.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import orgaplan.beratung.kreditunterlagen.Types;

import java.time.LocalDateTime;

@Entity
@Table(name = "kreditvermittler")
@Getter
@Setter
public class Kreditvermittler {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "first_name", nullable = false)
    @NotNull
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotNull
    private String lastName;

    @Column(name = "phone_number", nullable = false)
    @NotNull
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true)
    @NotNull
    private String email;

    @Column(name = "password", nullable = false, unique = true)
    @NotNull
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @NotNull
    private Types.UserRole role;

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

    @Column(name = "logo")
    private String logo;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
