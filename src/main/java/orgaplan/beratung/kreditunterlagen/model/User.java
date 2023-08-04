package orgaplan.beratung.kreditunterlagen.model;

import org.hibernate.annotations.GenericGenerator;

import javax.validation.constraints.NotNull; // Import the @NotNull annotation
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "first_name", nullable = false)
    @NotNull
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotNull
    private String lastName;

    @Column(name = "phone_number", nullable = false)
    @NotNull
    private String phoneNumber;

    @Column(name = "email", nullable = false)
    @NotNull
    private String email;

    @Column(name = "username", nullable = false)
    @NotNull
    private String username;

    @Column(name = "password", nullable = false)
    @NotNull
    private String password;

    @Column(name = "role", nullable = false)
    @NotNull
    private String role;

    @Column(name = "is_company_client", nullable = false)
    @NotNull
    private Boolean isCompanyClient;

    @Column(name = "privacy_policy_accepted", nullable = false)
    @NotNull
    private Boolean privacyPolicyAccepted;

    @Column(name = "terms_and_conditions_accepted", nullable = false)
    @NotNull
    private Boolean termsAndConditionsAccepted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
