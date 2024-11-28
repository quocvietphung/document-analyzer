package orgaplan.beratung.kreditunterlagen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import orgaplan.beratung.kreditunterlagen.dto.SelbstauskunftDocumentDTO;
import orgaplan.beratung.kreditunterlagen.model.Selbstauskunft;
import orgaplan.beratung.kreditunterlagen.model.User;
import java.util.Optional;

@Repository
public interface SelbstauskunftRepository extends JpaRepository<Selbstauskunft, String> {
    Optional<Selbstauskunft> findByUser(User user);

    @Query("SELECT new orgaplan.beratung.kreditunterlagen.dto.SelbstauskunftDocumentDTO(" +
            "d.id, " +
            "d.documentType, " +
            "d.fileName, " +
            "d.createdAt, " +
            "d.updatedAt, " +
            "s.status) " +
            "FROM Selbstauskunft s LEFT JOIN s.document d " +
            "WHERE s.user.id = :userId")
    Optional<SelbstauskunftDocumentDTO> findSelbstauskunftDocumentByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(s) > 0 FROM Selbstauskunft s WHERE s.user.id = :userId")
    boolean existsByUserId(@Param("userId") String userId);
    void deleteByUserId(String userId);
}