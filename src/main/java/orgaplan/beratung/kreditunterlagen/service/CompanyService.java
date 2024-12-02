package orgaplan.beratung.kreditunterlagen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import orgaplan.beratung.kreditunterlagen.model.Company;
import orgaplan.beratung.kreditunterlagen.repository.CompanyRepository;

import java.time.LocalDateTime;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CoordinatesService coordinatesService;

    public Company getCompanyByUserId(String userId) {
        Company company = companyRepository.findByUserId(userId);
        if (company == null) {
            throw new IllegalArgumentException("Unternehmen nicht gefunden für diese Benutzer-ID :: " + userId);
        }
        return company;
    }

    public Company updateCompany(String userId, Company companyRequest) {
        validateRequiredFields(companyRequest);
        Company existingCompany = companyRepository.findByUserId(userId);
        if (existingCompany == null) {
            throw new IllegalArgumentException("Unternehmen nicht gefunden für diese Benutzer-ID :: " + userId);
        }

        String location = coordinatesService.fetchCoordinates(
                companyRequest.getStreetNumber(),
                companyRequest.getPostalCode(),
                companyRequest.getCity(),
                companyRequest.getCountry()
        );

        existingCompany.setIsSelfEmployed(companyRequest.getIsSelfEmployed());
        existingCompany.setCompanyName(companyRequest.getCompanyName());
        existingCompany.setStreetNumber(companyRequest.getStreetNumber());
        existingCompany.setPostalCode(companyRequest.getPostalCode());
        existingCompany.setCity(companyRequest.getCity());
        existingCompany.setCountry(companyRequest.getCountry());
        existingCompany.setLocation(location);
        existingCompany.setIndustry(companyRequest.getIndustry());
        existingCompany.setNumberOfEmployees(companyRequest.getNumberOfEmployees());
        existingCompany.setCeo(companyRequest.getCeo());
        existingCompany.setCourt(companyRequest.getCourt());
        existingCompany.setCommercialRegisterNumber(companyRequest.getCommercialRegisterNumber());
        existingCompany.setVatId(companyRequest.getVatId());
        existingCompany.setCompanyEmail(companyRequest.getCompanyEmail());
        existingCompany.setPhone(companyRequest.getPhone());
        existingCompany.setFax(companyRequest.getFax());
        existingCompany.setWebsite(companyRequest.getWebsite());
        existingCompany.setUpdatedAt(LocalDateTime.now());

        return companyRepository.save(existingCompany);
    }

    private void validateRequiredFields(Company company) {
        if (company.getIsSelfEmployed() == null) {
            throw new IllegalArgumentException("Selbstständig-Feld ist erforderlich");
        }
        if (company.getCompanyName() == null || company.getCompanyName().trim().isEmpty()) {
            throw new IllegalArgumentException("Unternehmensname ist erforderlich");
        }
        if (company.getStreetNumber() == null || company.getStreetNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Straßennummer ist erforderlich");
        }
        if (company.getPostalCode() == null || company.getPostalCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Postleitzahl ist erforderlich");
        }
        if (company.getCity() == null || company.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("Stadt ist erforderlich");
        }
        if (company.getCountry() == null || company.getCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Land ist erforderlich");
        }
        if (company.getIndustry() == null || company.getIndustry().trim().isEmpty()) {
            throw new IllegalArgumentException("Industrie ist erforderlich");
        }
    }
}