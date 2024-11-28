package orgaplan.beratung.kreditunterlagen.repository;

import orgaplan.beratung.kreditunterlagen.model.Applicant;
import orgaplan.beratung.kreditunterlagen.model.Selbstauskunft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, String> {
    List<Applicant> findByLastName(String lastName);
    List<Applicant> findBySelbstauskunft(Selbstauskunft selbstauskunft);
}