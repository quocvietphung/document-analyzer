package orgaplan.beratung.kreditunterlagen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import orgaplan.beratung.kreditunterlagen.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Query("SELECT " +
            "COUNT(u) as totalClients, " +
            "COUNT(CASE WHEN u.isActive = true THEN 1 END) as activeClients, " +
            "COUNT(CASE WHEN u.role = 'PRIVAT_KUNDE' THEN 1 END) as numberOfPrivateClients, " +
            "COUNT(CASE WHEN u.role = 'FIRMEN_KUNDE' THEN 1 END) as numberOfBusinessClients, " +
            "COUNT(CASE WHEN u.documentUploadPercentage = 100.0 THEN 1 END) as clientsWithDocumentProcessComplete, " +
            "COUNT(CASE WHEN EXISTS (SELECT 1 FROM CreditRequest cr WHERE cr.user.id = u.id) THEN 1 END) as clientsWithKreditanfrage, " +
            "COUNT(CASE WHEN u.forwardedBanks = true THEN 1 END) as clientsForwardedToBank " +
            "FROM User u WHERE u.vermittlerId = :vermittlerId")
    List<Object[]> getClientStatistics(@Param("vermittlerId") String vermittlerId);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByOrderByCreatedAtAsc();
    List<User> findByVermittlerId(String vermittlerId);
    Optional<User> findByVermittlerIdAndId(String vermittlerId, String userId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.vermittlerId = :vermittlerId")
    long countByVermittlerId(@Param("vermittlerId") String vermittlerId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.vermittlerId = :vermittlerId AND u.createdAt >= :startOfWeek AND u.createdAt <= :endOfWeek")
    long countRegistrationsThisWeek(@Param("vermittlerId") String vermittlerId, @Param("startOfWeek") LocalDateTime startOfWeek, @Param("endOfWeek") LocalDateTime endOfWeek);
}