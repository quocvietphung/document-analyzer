package orgaplan.beratung.kreditunterlagen.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientStatisticsResponse {
    private long totalNumberOfClients;
    private long numberOfActiveClients;
    private long numberOfPrivateClients;
    private long numberOfBusinessClients;
    private long clientsWithCompletedDocumentProcess;
    private long clientsWithKreditanfrage;
    private long clientsForwardedToBank;
}