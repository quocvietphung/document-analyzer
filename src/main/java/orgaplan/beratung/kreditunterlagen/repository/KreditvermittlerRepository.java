package orgaplan.beratung.kreditunterlagen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orgaplan.beratung.kreditunterlagen.model.Kreditvermittler;

public interface KreditvermittlerRepository extends JpaRepository<Kreditvermittler, String> {

}
