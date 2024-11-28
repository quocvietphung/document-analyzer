package orgaplan.beratung.kreditunterlagen.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KreditvermittlerForm {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private Boolean privacyPolicyAccepted;
    private Boolean termsAndConditionsAccepted;
    private Boolean usageTermsAccepted;
    private Boolean consentTermsAccepted;
    private MultipartFile profileImage;
    private MultipartFile logo;
}
