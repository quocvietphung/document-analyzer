package ai.document.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadDto {
    private String id;
    private String fileName;
    private String documentType;
    private String blobUrl;
    private Long fileSize;
    private String uploadedAt;
}
