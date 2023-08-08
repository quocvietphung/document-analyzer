package orgaplan.beratung.kreditunterlagen.validation;

import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.Types;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DocumentValidation {

    public void validateDocumentTypeForUserRole(String documentType, User user) {
        List<String> allowedDocumentTypes;
        if (user.getRole().equals("PRIVAT_KUNDEN")) {
            allowedDocumentTypes = Types.PRIVAT_KUNDEN_DOCUMENTS;
        } else {
            allowedDocumentTypes = Types.FIRMEN_KUNDEN_DOCUMENTS;
        }

        if (!allowedDocumentTypes.contains(documentType)) {
            throw new IllegalArgumentException("Invalid document type: " + documentType + " for role: " + user.getRole());
        }
    }
}
