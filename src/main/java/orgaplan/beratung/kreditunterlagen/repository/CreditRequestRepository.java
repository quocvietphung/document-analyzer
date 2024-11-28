package orgaplan.beratung.kreditunterlagen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orgaplan.beratung.kreditunterlagen.model.CreditRequest;
import orgaplan.beratung.kreditunterlagen.model.User;
import java.util.List;

public interface CreditRequestRepository extends JpaRepository<CreditRequest, String> {
    List<CreditRequest> findByUserId(String userId);
    List<CreditRequest> findByUserOrderByCreatedAtDesc(User user);
    CreditRequest findByUserAndId(User user, String creditRequestId);
    void deleteByUserId(String userId);
    Long countByUserId(String userId);
}
