package orgaplan.beratung.kreditunterlagen.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.request.CreateUserRequest;
import orgaplan.beratung.kreditunterlagen.service.UserService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ein Benutzer mit dieser E-Mail existiert bereits");
        }

        User user = userService.createUser(request);
        return ResponseEntity.ok(Map.of("user", user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        User user = userService.login(email, password);
        return ResponseEntity.ok(Map.of("user", user));
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody CreateUserRequest request) {
        User updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/upload-percentage")
    public ResponseEntity<?> savePercentageUploaded(
            @RequestParam String userId,
            @RequestParam BigDecimal percentageUploaded) {
        userService.updateUploadPercentage(userId, percentageUploaded);
        return ResponseEntity.ok(Map.of("message", "Upload percentage updated successfully"));
    }
}