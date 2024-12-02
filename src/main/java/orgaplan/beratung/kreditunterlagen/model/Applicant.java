package orgaplan.beratung.kreditunterlagen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import orgaplan.beratung.kreditunterlagen.Types;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "applicant")
@Getter
@Setter
public class Applicant {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selbstauskunft_id", referencedColumnName = "id", nullable = false)
    @NotNull
    private Selbstauskunft selbstauskunft;

    @Column(name = "first_name", nullable = false, length = 255)
    @NotNull
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 255)
    @NotNull
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @NotNull
    private Types.ApplicantRole role;

    @Column(name = "salutation", nullable = false)
    @NotNull
    private String salutation;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "date_of_birth", nullable = false)
    @NotNull
    private Date dateOfBirth;

    @Column(name = "place_of_birth", nullable = false, length = 255)
    @NotNull
    private String placeOfBirth;

    @Column(name = "country_of_birth", nullable = false, length = 255)
    @NotNull
    private String countryOfBirth;

    @Column(name = "nationality", nullable = false, length = 255)
    @NotNull
    private String nationality;

    @Column(name = "second_nationality", length = 255)
    private String secondNationality;

    @Column(name = "phone", nullable = false, length = 50)
    @NotNull
    private String phone;

    @Column(name = "email", nullable = false, length = 255)
    @NotNull
    private String email;

    @Column(name = "street_house_number", nullable = false, length = 255)
    @NotNull
    private String streetHouseNumber;

    @Column(name = "postal_code", nullable = false, length = 10)
    @NotNull
    private String postalCode;

    @Column(name = "city", nullable = false, length = 255)
    @NotNull
    private String city;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "location", length = 50)
    private String location;

    @Column(name = "residence_since")
    private Date residenceSince;

    @Column(name = "marital_status", nullable = false, length = 50)
    @NotNull
    private String maritalStatus;

    @Column(name = "tax_id", nullable = false, length = 50)
    @NotNull
    private String taxId;

    @Column(name = "employment_type", nullable = false, length = 50)
    @NotNull
    private String employmentType;

    @Column(name = "net_income", nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal netIncome;

    @Column(name = "disposable_income", nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal disposableIncome;

    @Column(name = "statutory_pension", precision = 15, scale = 2)
    private BigDecimal statutoryPension;

    @Column(name = "private_pension", precision = 15, scale = 2)
    private BigDecimal privatePension;

    @Column(name = "other_income", precision = 15, scale = 2)
    private BigDecimal otherIncome;

    @Column(name = "employer_name", nullable = false, length = 255)
    @NotNull
    private String employerName;

    @Column(name = "job_title", length = 255)
    private String jobTitle;

    @Column(name = "employer_based_in_germany", nullable = false)
    @NotNull
    private Boolean employerBasedInGermany;

    @Column(name = "employment_fixed_until")
    private Date employmentFixedUntil;

    @Column(name = "probation_until")
    private Date probationUntil;

    @Column(name = "retirement_date")
    private Date retirementDate;

    @Column(name = "credit_institute", nullable = false, length = 255)
    @NotNull
    private String creditInstitute;

    @Column(name = "iban", nullable = false, length = 34)
    @NotNull
    private String iban;

    @Column(name = "bic", nullable = false, length = 11)
    @NotNull
    private String bic;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}