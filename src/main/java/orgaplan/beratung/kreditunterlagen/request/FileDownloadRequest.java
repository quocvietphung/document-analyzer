package orgaplan.beratung.kreditunterlagen.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDownloadRequest {
    private String userId;
    private String documentType;
    private String fileName;
}
