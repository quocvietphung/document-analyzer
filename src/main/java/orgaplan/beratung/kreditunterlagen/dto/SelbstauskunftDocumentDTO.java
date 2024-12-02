package orgaplan.beratung.kreditunterlagen.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import orgaplan.beratung.kreditunterlagen.Types;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelbstauskunftDocumentDTO {
    private String id;
    private Types.DocumentType documentType;
    private String fileName;
    private Types.SelbstauskunftStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SelbstauskunftDocumentDTO(String id, Types.DocumentType documentType, String fileName, LocalDateTime createdAt, LocalDateTime updatedAt, Types.SelbstauskunftStatus status) {
        this.id = id;
        this.documentType = documentType;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }
}
