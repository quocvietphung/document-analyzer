package orgaplan.beratung.kreditunterlagen.repository;

import orgaplan.beratung.kreditunterlagen.Types;
import orgaplan.beratung.kreditunterlagen.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByUserId(String userId);
    Optional<Document> findByIdAndUserId(String documentId, String userId);
}

