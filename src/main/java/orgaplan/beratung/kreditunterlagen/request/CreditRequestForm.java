package orgaplan.beratung.kreditunterlagen.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import orgaplan.beratung.kreditunterlagen.validation.ValidationGroups;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditRequestForm {
    @NotNull(message = "ID ist für die Aktualisierung erforderlich", groups = ValidationGroups.Update.class)
    private String id;

    @NotNull(message = "KreditTyp ist erforderlich")
    @Size(min = 1, message = "KreditTyp darf nicht leer sein")
    private String kreditTyp;

    private String kreditLink;

    @NotNull(message = "Betrag ist erforderlich")
    @DecimalMin(value = "10000.0", message = "Betrag muss mindestens 10000 sein")
    private BigDecimal betrag;

    @NotNull(message = "Laufzeit ist erforderlich")
    @Min(value = 1, message = "Laufzeit muss mindestens 1 Jahr sein")
    private Integer laufzeit;
}