package orgaplan.beratung.kreditunterlagen.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "privacy_policy_accepted", nullable = false)
    private Boolean privacyPolicyAccepted;

    @Column(name = "terms_and_conditions_accepted", nullable = false)
    private Boolean termsAndConditionsAccepted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
