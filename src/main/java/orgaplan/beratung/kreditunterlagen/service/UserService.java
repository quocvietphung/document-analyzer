package orgaplan.beratung.kreditunterlagen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.UserRepository;

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

    @Transactional
    public User createUser(User user) {
        validateRequiredFields(user);
        user.setId(UUID.randomUUID().toString()); // Generate a unique ID
        hashPassword(user);
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
        hashPasswordIfPresent(updatedUserFields);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    // Helper methods
    private void validateRequiredFields(User user) {
        // Implement the logic to check required fields and throw an exception if any field is missing.
    }

    private void hashPassword(User user) {
        // Implement the logic to hash the user's password before saving it.
    }

    private void updateFields(User user, User updatedUserFields) {
        // Implement the logic to update user fields based on the values in updatedUserFields.
    }

    private void hashPasswordIfPresent(User updatedUserFields) {
        // Implement the logic to hash the password in updatedUserFields if it is present.
    }

    // Other methods as needed
}
