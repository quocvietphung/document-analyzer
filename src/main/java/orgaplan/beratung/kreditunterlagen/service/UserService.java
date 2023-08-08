package orgaplan.beratung.kreditunterlagen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && isPasswordCorrect(user, password)) {
            return user;
        }
        return null;
    }

    private boolean isPasswordCorrect(User user, String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(password, user.getPassword());
    }

    public User createUser(User user) {
        validateRequiredFields(user);
        user.setId(UUID.randomUUID().toString()); // Generate a unique ID
        user.setCreatedAt(LocalDateTime.now()); // Set created at timestamp
        user.setUpdatedAt(LocalDateTime.now()); // Set updated at timestamp
        hashPassword(user);

        if (user.getIsCompanyClient()) {
            user.setRole("FIRMEN_KUNDEN");
        } else {
            user.setRole("PRIVAT_KUNDEN");
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
    }

    @Transactional
    public User editUser(String id, User updatedUserFields) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));

        updateFields(user, updatedUserFields);
        if (updatedUserFields.getPassword() != null) {
            user.setPassword(updatedUserFields.getPassword());
            hashPassword(user);
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    // Helper methods
    private void validateRequiredFields(User user) {
        if (user.getFirstName() == null || user.getLastName() == null || user.getEmail() == null
                || user.getPhoneNumber() == null || user.getUsername() == null || user.getPassword() == null
                || user.getPrivacyPolicyAccepted() == null || user.getTermsAndConditionsAccepted() == null
                || user.getIsCompanyClient() == null) {
            throw new IllegalArgumentException("Missing required fields");
        }
    }

    private void hashPassword(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
    }

    private void updateFields(User user, User updatedUserFields) {
        if (updatedUserFields.getFirstName() != null) user.setFirstName(updatedUserFields.getFirstName());
        if (updatedUserFields.getLastName() != null) user.setLastName(updatedUserFields.getLastName());
        if (updatedUserFields.getEmail() != null) user.setEmail(updatedUserFields.getEmail());
        if (updatedUserFields.getPhoneNumber() != null) user.setPhoneNumber(updatedUserFields.getPhoneNumber());
        if (updatedUserFields.getCompanyName() != null) user.setCompanyName(updatedUserFields.getCompanyName());
        if (updatedUserFields.getUsername() != null) user.setUsername(updatedUserFields.getUsername());
        if (updatedUserFields.getRole() != null) user.setRole(updatedUserFields.getRole());
        if (updatedUserFields.getPrivacyPolicyAccepted() != null) user.setPrivacyPolicyAccepted(updatedUserFields.getPrivacyPolicyAccepted());
        if (updatedUserFields.getTermsAndConditionsAccepted() != null) user.setTermsAndConditionsAccepted(updatedUserFields.getTermsAndConditionsAccepted());

        user.setUpdatedAt(LocalDateTime.now());
    }
}
