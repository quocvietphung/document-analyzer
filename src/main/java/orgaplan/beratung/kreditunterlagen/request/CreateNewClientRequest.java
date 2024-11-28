package orgaplan.beratung.kreditunterlagen.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import orgaplan.beratung.kreditunterlagen.Types;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNewClientRequest {
    private String companyName;
    private String streetNumber;
    private String postalCode;
    private String city;
    private String country;
    private String location;
    private String industry;
    private String numberOfEmployees;
    private String ceo;
    private String court;
    private String commercialRegisterNumber;
    private String vatId;
    private String companyEmail;
    private String phone;
    private String fax;
    private String website;
    private String vermittlerId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String username;
    private String password;
    private String role;
    private Boolean isSelfEmployed;
    private Boolean termsAndConditionsAccepted;
    private Boolean privacyPolicyAccepted;
    private Boolean usageTermsAccepted;
    private Boolean consentTermsAccepted;
}
