package orgaplan.beratung.kreditunterlagen.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import orgaplan.beratung.kreditunterlagen.Types;
import orgaplan.beratung.kreditunterlagen.model.Kreditvermittler;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDetail {
    private String id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private Types.UserRole role;
    private Boolean termsAndConditionsAccepted;
    private Boolean privacyPolicyAccepted;
    private Boolean usageTermsAccepted;
    private Boolean consentTermsAccepted;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isActive;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime forwardedBanksAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal documentProgress;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean hasKreditanfrage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean withSecondPartner;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean forwardedBanks;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String logo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profileImage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Kreditvermittler vermittler;
}