package orgaplan.beratung.kreditunterlagen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    @JsonBackReference
    private User user;

    @NotNull(message = "Die Selbstständigkeit darf nicht null sein")
    @Column(name = "is_self_employed", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isSelfEmployed;

    @NotNull(message = "Der Firmenname darf nicht null sein")
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @NotNull(message = "Die Hausnummer darf nicht null sein")
    @Column(name = "street_number", nullable = false)
    private String streetNumber;

    @NotNull(message = "Die Postleitzahl darf nicht null sein")
    @Column(name = "postal_code", nullable = false, length = 50)
    private String postalCode;

    @NotNull(message = "Die Stadt darf nicht null sein")
    @Column(name = "city", nullable = false)
    private String city;

    @NotNull(message = "Das Land darf nicht null sein")
    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "location", length = 50)
    private String location;

    @NotNull(message = "Die Branche darf nicht null sein")
    @Column(name = "industry", nullable = false)
    private String industry;

    @Column(name = "number_of_employees", length = 50)
    private String numberOfEmployees;

    @Column(name = "ceo")
    private String ceo;

    @Column(name = "court")
    private String court;

    @Column(name = "commercial_register_number")
    private String commercialRegisterNumber;

    @Column(name = "vat_id", length = 50)
    private String vatId;

    @Email(message = "Die Firmen-E-Mail muss ein gültiges Format haben")
    @Column(name = "company_email")
    private String companyEmail;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "fax", length = 50)
    private String fax;

    @Column(name = "website")
    private String website;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}