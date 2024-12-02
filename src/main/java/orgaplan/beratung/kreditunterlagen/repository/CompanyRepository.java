package orgaplan.beratung.kreditunterlagen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import orgaplan.beratung.kreditunterlagen.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    Company findByUserId(String userId);
    boolean existsByUserId(String userId);
    void deleteByUserId(String userId);
}
