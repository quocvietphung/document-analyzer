package orgaplan.beratung.kreditunterlagen.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import orgaplan.beratung.kreditunterlagen.model.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    private List<Document> documents;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isSelfEmployed;
}