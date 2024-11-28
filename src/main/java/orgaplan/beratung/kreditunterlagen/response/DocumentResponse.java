package orgaplan.beratung.kreditunterlagen.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import orgaplan.beratung.kreditunterlagen.dto.SelbstauskunftDocumentDTO;
import orgaplan.beratung.kreditunterlagen.model.Document;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    private Map<String, List<Document>> regularDocuments = new HashMap<>();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, List<Document>> ehepartnerDocuments = new HashMap<>();
    private SelbstauskunftDocumentDTO selbstauskunftDocument;
    private Map<String, List<Document>> optionDocuments = new HashMap<>();
    private BigDecimal percentageUploaded;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isSelfEmployed;
}