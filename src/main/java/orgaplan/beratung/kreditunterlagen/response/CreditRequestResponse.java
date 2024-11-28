package orgaplan.beratung.kreditunterlagen.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import orgaplan.beratung.kreditunterlagen.Types;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditRequestResponse {
    private String id;
    private Types.KreditTyp kreditTyp;
    private String kreditLink;
    private BigDecimal betrag;
    private Integer laufzeit;
}