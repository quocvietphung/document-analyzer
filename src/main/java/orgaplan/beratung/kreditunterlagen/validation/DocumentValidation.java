package orgaplan.beratung.kreditunterlagen.validation;

import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.Types;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class DocumentValidation {

    public void validateDocumentTypeForUserRole(String documentType, User user) {
        List<String> allowedDocumentTypes = new ArrayList<>();

        if (user.getRole() == Types.UserRole.PRIVAT_KUNDE) {
            allowedDocumentTypes.addAll(Types.getPrivatKundenDocuments());
        } else if (user.getRole() == Types.UserRole.FIRMEN_KUNDE) {
            allowedDocumentTypes.addAll(Types.getFirmenKundenDocuments());
        }

        if (user.getWithSecondPartner()) {
            allowedDocumentTypes.addAll(Types.getEhepartnerDocuments());
        }

        if (!allowedDocumentTypes.contains(documentType) && !documentType.equals(Types.DocumentType.SONSTIGE_DOKUMENTE.name())) {
            throw new IllegalArgumentException("Invalid document type: " + documentType + " for role: " + user.getRole());
        }
    }
}