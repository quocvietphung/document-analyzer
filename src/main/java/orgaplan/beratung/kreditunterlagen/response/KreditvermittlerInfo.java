package orgaplan.beratung.kreditunterlagen.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import orgaplan.beratung.kreditunterlagen.Types;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class KreditvermittlerInfo {
    private String firstName;
    private String lastName;
    private Types.UserRole role;
    private String email;
    private String phoneNumber;
}
