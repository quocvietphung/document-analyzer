package orgaplan.beratung.kreditunterlagen.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String role;
    private boolean active;
    private boolean withSecondPartner;
    private double documentProgress;
    private boolean hasKreditanfrage;
    private boolean forwardedBanks;
    private LocalDateTime forwardedBanksAt;
    private LocalDateTime createdAt;
}