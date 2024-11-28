package orgaplan.beratung.kreditunterlagen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelbstauskunftDTO {
    private String id;

    @NotNull(message = "Anzahl der Kinder darf nicht leer sein")
    private Integer numberOfChildren;

    private String status;

    private LocalDateTime applicationDate;

    @NotEmpty(message = "Liste der Antragsteller darf nicht leer sein")
    private List<@Valid ApplicantDTO> applicants;

    private List<@Valid ChildDTO> children;
}