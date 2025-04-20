package orgaplan.beratung.kreditunterlagen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import orgaplan.beratung.kreditunterlagen.Types;
import orgaplan.beratung.kreditunterlagen.model.Company;
import orgaplan.beratung.kreditunterlagen.model.Kreditvermittler;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.*;
import orgaplan.beratung.kreditunterlagen.request.CreateNewClientRequest;
import orgaplan.beratung.kreditunterlagen.response.UserDetail;
import orgaplan.beratung.kreditunterlagen.util.Util;
import orgaplan.beratung.kreditunterlagen.validation.UserRoleValidation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KreditvermittlerRepository kreditvermittlerRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private WebClient webClient;

    @Autowired
    private CreditRequestRepository creditRequestRepository;

    @Autowired
    private CoordinatesService coordinatesService;

    @Transactional(readOnly = true)
    public User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer mit ID: " + id + " wurde nicht gefunden"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Kreditvermittler findKreditvermittlerById(String id) {
        return kreditvermittlerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kreditvermittler mit ID: " + id + " wurde nicht gefunden"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validateRequiredFields(User user) {
        if (user.getFirstName() == null || user.getLastName() == null || user.getEmail() == null
                || user.getPhoneNumber() == null || user.getRole() == null) {
            throw new IllegalArgumentException("Fehlende erforderliche Felder oder nicht akzeptierte Richtlinien");
        }
    }

    private void validateCompanyFields(CreateNewClientRequest request) {
        if (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty()
                || request.getStreetNumber() == null || request.getStreetNumber().trim().isEmpty()
                || request.getPostalCode() == null || request.getPostalCode().trim().isEmpty()
                || request.getCity() == null || request.getCity().trim().isEmpty()
                || request.getCountry() == null || request.getCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Fehlende erforderliche Unternehmensfelder");
        }
    }

    public User createNewClient(CreateNewClientRequest request) {
        UserRoleValidation.validateRole(request.getRole());
        Types.UserRole role = Types.UserRole.valueOf(request.getRole());

        if (Types.UserRole.FIRMEN_KUNDE == role) {
            validateCompanyFields(request);
        }

        String responseUUID = UUID.randomUUID().toString();

        User user = User.builder()
                .id(responseUUID)
                .vermittlerId(request.getVermittlerId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(role)
                .withSecondPartner(false)
                .isActive(false)
                .documentUploadPercentage(BigDecimal.ZERO)
                .forwardedBanks(false)
                .termsAndConditionsAccepted(request.getTermsAndConditionsAccepted())
                .privacyPolicyAccepted(request.getPrivacyPolicyAccepted())
                .usageTermsAccepted(request.getUsageTermsAccepted())
                .consentTermsAccepted(request.getConsentTermsAccepted())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validateRequiredFields(user);

        User savedUser = userRepository.save(user);

        if (Types.UserRole.FIRMEN_KUNDE == role) {
            if (companyRepository.existsByUserId(responseUUID)) {
                throw new IllegalStateException("Ein Unternehmen, das mit dieser Benutzer-ID verknüpft ist, existiert bereits.");
            }

            String location = coordinatesService.fetchCoordinates(
                    request.getStreetNumber(),
                    request.getPostalCode(),
                    request.getCity(),
                    request.getCountry()
            );

            Company company = Company.builder()
                    .user(savedUser)
                    .isSelfEmployed(request.getIsSelfEmployed())
                    .companyName(request.getCompanyName())
                    .streetNumber(request.getStreetNumber())
                    .postalCode(request.getPostalCode())
                    .city(request.getCity())
                    .country(request.getCountry())
                    .location(location)
                    .industry(request.getIndustry())
                    .numberOfEmployees(request.getNumberOfEmployees())
                    .ceo(request.getCeo())
                    .court(request.getCourt())
                    .commercialRegisterNumber(request.getCommercialRegisterNumber())
                    .vatId(request.getVatId())
                    .companyEmail(request.getCompanyEmail())
                    .phone(request.getPhone())
                    .fax(request.getFax())
                    .website(request.getWebsite())
                    .build();

            companyRepository.save(company);
        }

        return savedUser;
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAllByOrderByCreatedAtAsc();
    }

    public UserDetail convertKreditvermittlerToUserDetail(Kreditvermittler kreditvermittler) {
        return UserDetail.builder()
                .id(kreditvermittler.getId())
                .firstName(kreditvermittler.getFirstName())
                .lastName(kreditvermittler.getLastName())
                .phoneNumber(kreditvermittler.getPhoneNumber())
                .email(kreditvermittler.getEmail())
                .password(kreditvermittler.getPassword())
                .role(kreditvermittler.getRole())
                .privacyPolicyAccepted(kreditvermittler.getPrivacyPolicyAccepted())
                .termsAndConditionsAccepted(kreditvermittler.getTermsAndConditionsAccepted())
                .usageTermsAccepted(kreditvermittler.getUsageTermsAccepted())
                .consentTermsAccepted(kreditvermittler.getConsentTermsAccepted())
                .logo(kreditvermittler.getLogo())
                .profileImage(kreditvermittler.getProfileImage())
                .build();
    }

    @Transactional
    public UserDetail getUserById(String userId) {
        User user = findUserById(userId);
        System.out.println("User: " + user);
        Kreditvermittler vermittler = null;
        if (user.getVermittlerId() != null && !user.getVermittlerId().isEmpty()) {
            vermittler = findKreditvermittlerById(user.getVermittlerId());
        }

        Long creditRequestCount = creditRequestRepository.countByUserId(userId);
        boolean hasKreditanfrage = creditRequestCount > 0;
        return UserDetail.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .termsAndConditionsAccepted(user.getTermsAndConditionsAccepted())
                .privacyPolicyAccepted(user.getPrivacyPolicyAccepted())
                .usageTermsAccepted(user.getUsageTermsAccepted())
                .consentTermsAccepted(user.getConsentTermsAccepted())
                .withSecondPartner(user.getWithSecondPartner())
                .hasKreditanfrage(hasKreditanfrage)
                .forwardedBanks(user.getForwardedBanks())
                .forwardedBanksAt(user.getForwardedBanksAt())
                .documentProgress(user.getDocumentUploadPercentage())
                .vermittler(vermittler)
                .build();
    }

    @Transactional
    public User editUser(String userId, User updatedUserFields) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("Benutzer-ID fehlt oder ist ungültig");
        }

        if (updatedUserFields.getId() != null && !userId.equals(updatedUserFields.getId())) {
            throw new IllegalArgumentException("Benutzer-ID im Pfad stimmt nicht mit der ID im Anforderungstext überein");
        }

        User user = findUserById(userId);

        updateFields(user, updatedUserFields);

        return userRepository.save(user);
    }

    private void updateFields(User user, User updatedUserFields) {
        user.setFirstName(updatedUserFields.getFirstName() != null ? updatedUserFields.getFirstName() : user.getFirstName());
        user.setLastName(updatedUserFields.getLastName() != null ? updatedUserFields.getLastName() : user.getLastName());
        user.setEmail(updatedUserFields.getEmail() != null ? updatedUserFields.getEmail() : user.getEmail());
        user.setPhoneNumber(updatedUserFields.getPhoneNumber() != null ? updatedUserFields.getPhoneNumber() : user.getPhoneNumber());
        user.setRole(updatedUserFields.getRole() != null ? updatedUserFields.getRole() : user.getRole());
        user.setIsActive(updatedUserFields.getIsActive() != null ? updatedUserFields.getIsActive() : user.getIsActive());
        user.setTermsAndConditionsAccepted(updatedUserFields.getTermsAndConditionsAccepted() != null ? updatedUserFields.getTermsAndConditionsAccepted() : user.getTermsAndConditionsAccepted());
        user.setPrivacyPolicyAccepted(updatedUserFields.getPrivacyPolicyAccepted() != null ? updatedUserFields.getPrivacyPolicyAccepted() : user.getPrivacyPolicyAccepted());
        user.setUsageTermsAccepted(updatedUserFields.getUsageTermsAccepted() != null ? updatedUserFields.getUsageTermsAccepted() : user.getUsageTermsAccepted());
        user.setConsentTermsAccepted(updatedUserFields.getConsentTermsAccepted() != null ? updatedUserFields.getConsentTermsAccepted() : user.getConsentTermsAccepted());
        user.setUpdatedAt(LocalDateTime.now());
    }

    public void updateUploadPercentage(String userId, BigDecimal percentageUploaded) {
        User user = findUserById(userId);
        user.setDocumentUploadPercentage(percentageUploaded);
        userRepository.save(user);
    }

    public void updateSecondPartner(String userId, Boolean withSecondPartner) {
        User user = findUserById(userId);
        if (user.getForwardedBanks()) {
            throw new RuntimeException("Die Weiterleitung zu den Banken wurde aktiviert, daher können Sie die Information zum zweiten Partner nicht aktualisieren.");
        }
        user.setWithSecondPartner(withSecondPartner);
        userRepository.save(user);
    }
}