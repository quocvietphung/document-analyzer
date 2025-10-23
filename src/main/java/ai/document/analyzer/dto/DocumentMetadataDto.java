package ai.document.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadataDto {
    private String id;
    private String fileName;
    private String documentType;
    private String blobUrl;
    private String createdAt;
    private String updatedAt;
    private String userId;
}
