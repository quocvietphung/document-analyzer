package orgaplan.beratung.kreditunterlagen.service;

import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import orgaplan.beratung.kreditunterlagen.enums.UserRole;
import orgaplan.beratung.kreditunterlagen.model.Kreditvermittler;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.*;
import orgaplan.beratung.kreditunterlagen.request.CreateUserRequest;
import orgaplan.beratung.kreditunterlagen.validation.UserRoleValidation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KreditvermittlerRepository kreditvermittlerRepository;

    @Transactional(readOnly = true)
    public User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer mit ID: " + id + " wurde nicht gefunden"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User createUser(CreateUserRequest request) {
        UserRoleValidation.validateRole(request.getRole());

        UserRole role = UserRole.valueOf(request.getRole());

        if (role == UserRole.SUPER_ADMIN && userRepository.existsByRole(UserRole.SUPER_ADMIN)) {
            throw new IllegalStateException("Nur ein SUPER_ADMIN ist erlaubt.");
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .password(new BCryptPasswordEncoder().encode(request.getPassword()))
                .role(role)
                .isActive(false)
                .documentUploadPercentage(BigDecimal.ZERO)
                .termsAndConditionsAccepted(request.getTermsAndConditionsAccepted())
                .privacyPolicyAccepted(request.getPrivacyPolicyAccepted())
                .usageTermsAccepted(request.getUsageTermsAccepted())
                .consentTermsAccepted(request.getConsentTermsAccepted())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAllByOrderByCreatedAtAsc();
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

    @Transactional(readOnly = true)
    public Optional<Kreditvermittler> findOptionalKreditvermittlerById(String id) {
        return kreditvermittlerRepository.findById(id);
    }

}