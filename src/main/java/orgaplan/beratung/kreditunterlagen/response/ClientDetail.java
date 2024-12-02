package orgaplan.beratung.kreditunterlagen.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import orgaplan.beratung.kreditunterlagen.model.Company;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDetail {
    private ClientResponse client;
    private DocumentResponse document;
    private List<CreditRequestResponse> creditRequests;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Company company;
}