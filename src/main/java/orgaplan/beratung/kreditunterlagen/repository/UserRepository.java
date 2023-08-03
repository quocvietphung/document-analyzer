package orgaplan.beratung.kreditunterlagen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import orgaplan.beratung.kreditunterlagen.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Custom query methods (if needed)
}
