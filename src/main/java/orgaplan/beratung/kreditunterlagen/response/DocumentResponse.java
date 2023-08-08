package orgaplan.beratung.kreditunterlagen.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import orgaplan.beratung.kreditunterlagen.model.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private String userid;
    private Map<String, List<Document>> documents = new HashMap<>();
}
