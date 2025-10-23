package ai.document.analyzer.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ai.document.analyzer.model.User;
import ai.document.analyzer.request.CreateUserRequest;
import ai.document.analyzer.request.LoginRequest;
import ai.document.analyzer.service.UserService;

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

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        
        String userId = authentication.getName();
        User user = userService.findUserById(userId);
        return ResponseEntity.ok(user);
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
}