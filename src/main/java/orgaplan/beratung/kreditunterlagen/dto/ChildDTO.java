package orgaplan.beratung.kreditunterlagen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildDTO {
    private String id;

    @NotBlank(message = "Name darf nicht leer sein")
    private String name;

    @Past(message = "Geburtsdatum muss in der Vergangenheit liegen")
    @NotNull(message = "Geburtsdatum darf nicht leer sein")
    private Date dateOfBirth;

    @NotNull(message = "Kindergeld darf nicht leer sein")
    private Boolean childBenefit;

    @NotNull(message = "Unterhaltszahlungen dürfen nicht leer sein")
    private Boolean alimonyPayments;

    private BigDecimal monthlyAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}