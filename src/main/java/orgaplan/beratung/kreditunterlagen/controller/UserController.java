package orgaplan.beratung.kreditunterlagen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        User user = userService.login(username, password);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Invalid credentials");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> sayHello() {
        String message = "Hello, World!";
        return ResponseEntity.ok(message);
    }

    @PostMapping("/createUser")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        User newUser = userService.createUser(user);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("user", newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> editUser(@PathVariable String id, @RequestBody User updatedUserFields) {
        User updatedUser = userService.editUser(id, updatedUserFields);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
