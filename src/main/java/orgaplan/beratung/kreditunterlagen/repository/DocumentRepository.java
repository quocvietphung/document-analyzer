package orgaplan.beratung.kreditunterlagen.repository;

import orgaplan.beratung.kreditunterlagen.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Document findByUserIdAndDocumentType(String userId, String documentType);
}
