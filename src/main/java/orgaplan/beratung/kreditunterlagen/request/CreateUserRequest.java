package orgaplan.beratung.kreditunterlagen.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String password;
    private String role;
    private Boolean termsAndConditionsAccepted;
    private Boolean privacyPolicyAccepted;
    private Boolean usageTermsAccepted;
    private Boolean consentTermsAccepted;
}