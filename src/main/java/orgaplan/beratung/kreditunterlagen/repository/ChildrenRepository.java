package orgaplan.beratung.kreditunterlagen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import orgaplan.beratung.kreditunterlagen.model.Child;
import orgaplan.beratung.kreditunterlagen.model.Selbstauskunft;

import java.util.List;

@Repository
public interface ChildrenRepository extends JpaRepository<Child, String> {
    List<Child> findBySelbstauskunft(Selbstauskunft selbstauskunft);
    void deleteBySelbstauskunftId(String selbstauskunftId);
}
