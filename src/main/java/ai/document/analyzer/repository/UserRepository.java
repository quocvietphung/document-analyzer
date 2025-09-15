package ai.document.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.document.analyzer.model.User;
import ai.document.analyzer.enums.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);

    Optional<User> findByEmail(String email);

    List<User> findAllByOrderByCreatedAtAsc();
}