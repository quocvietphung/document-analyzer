package orgaplan.beratung.kreditunterlagen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import orgaplan.beratung.kreditunterlagen.enums.UserRole;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.UserRepository;
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

    @Transactional(readOnly = true)
    public User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer mit ID " + id + " wurde nicht gefunden"));
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
                .isActive(true)
                .termsAndConditionsAccepted(request.getTermsAndConditionsAccepted())
                .privacyPolicyAccepted(request.getPrivacyPolicyAccepted())
                .usageTermsAccepted(request.getUsageTermsAccepted())
                .consentTermsAccepted(request.getConsentTermsAccepted())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden mit E-Mail: " + email));

        if (!new BCryptPasswordEncoder().matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Falsches Passwort.");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAllByOrderByCreatedAtAsc();
    }

    public User updateUser(String id, CreateUserRequest request) {
        User user = findUserById(id);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}