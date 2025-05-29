package orgaplan.beratung.kreditunterlagen.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Vorname darf nicht leer sein")
    private String firstName;

    @NotBlank(message = "Nachname darf nicht leer sein")
    private String lastName;

    @NotBlank(message = "Telefonnummer darf nicht leer sein")
    private String phoneNumber;

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Ungültige E-Mail-Adresse")
    private String email;

    @NotBlank(message = "Passwort darf nicht leer sein")
    @Size(min = 6, message = "Passwort muss mindestens 6 Zeichen lang sein")
    private String password;

    @NotBlank(message = "Rolle darf nicht leer sein")
    private String role;

    @NotNull(message = "AGB müssen akzeptiert werden")
    private Boolean termsAndConditionsAccepted;

    @NotNull(message = "Datenschutzrichtlinie muss akzeptiert werden")
    private Boolean privacyPolicyAccepted;

    @NotNull(message = "Nutzungsbedingungen müssen akzeptiert werden")
    private Boolean usageTermsAccepted;

    @NotNull(message = "Einverständnis muss erteilt werden")
    private Boolean consentTermsAccepted;
}