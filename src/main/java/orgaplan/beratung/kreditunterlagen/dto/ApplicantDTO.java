package orgaplan.beratung.kreditunterlagen.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantDTO {
    private String id;

    @NotBlank(message = "Vorname darf nicht leer sein")
    private String firstName;

    @NotBlank(message = "Nachname darf nicht leer sein")
    private String lastName;

    private String role;

    @NotBlank(message = "Anrede darf nicht leer sein")
    private String salutation;

    private String title;

    @Past(message = "Geburtsdatum muss in der Vergangenheit liegen")
    @NotNull(message = "Geburtsdatum darf nicht leer sein")
    private Date dateOfBirth;

    @NotBlank(message = "Geburtsort darf nicht leer sein")
    private String placeOfBirth;

    @NotBlank(message = "Geburtsland darf nicht leer sein")
    private String countryOfBirth;

    @NotBlank(message = "Nationalität darf nicht leer sein")
    private String nationality;

    private String secondNationality;

    @Pattern(regexp = "^\\+?[0-9]+$", message = "Telefonnummer muss numerisch sein")
    private String phone;

    @Email(message = "E-Mail muss gültig sein")
    private String email;

    @NotBlank(message = "Straße und Hausnummer dürfen nicht leer sein")
    private String streetHouseNumber;

    @NotBlank(message = "Postleitzahl darf nicht leer sein")
    private String postalCode;

    @NotBlank(message = "Stadt darf nicht leer sein")
    private String city;

    @NotBlank(message = "Land darf nicht leer sein")
    private String country;

    private String location;

    private Date residenceSince;

    @NotBlank(message = "Familienstand darf nicht leer sein")
    private String maritalStatus;

    @NotBlank(message = "Steuer-ID darf nicht leer sein")
    private String taxId;

    @NotBlank(message = "Beschäftigungsart darf nicht leer sein")
    private String employmentType;

    @NotNull(message = "Nettoeinkommen darf nicht leer sein")
    private BigDecimal netIncome;

    @NotNull(message = "Verfügbares Einkommen darf nicht leer sein")
    private BigDecimal disposableIncome;

    private BigDecimal statutoryPension;
    private BigDecimal privatePension;
    private BigDecimal otherIncome;

    @NotBlank(message = "Arbeitgebername darf nicht leer sein")
    private String employerName;

    private String jobTitle;

    @NotNull(message = "Arbeitgeber muss in Deutschland ansässig sein")
    private Boolean employerBasedInGermany;

    private Date employmentFixedUntil;
    private Date probationUntil;
    private Date retirementDate;

    @NotBlank(message = "Kreditinstitut darf nicht leer sein")
    private String creditInstitute;

    @NotBlank(message = "IBAN darf nicht leer sein")
    private String iban;

    @NotBlank(message = "BIC darf nicht leer sein")
    private String bic;
}