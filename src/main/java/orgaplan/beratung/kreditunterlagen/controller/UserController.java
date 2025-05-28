package orgaplan.beratung.kreditunterlagen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import orgaplan.beratung.kreditunterlagen.Types;
import orgaplan.beratung.kreditunterlagen.model.Kreditvermittler;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.request.CreateUserRequest;
import orgaplan.beratung.kreditunterlagen.response.UserDetail;
import orgaplan.beratung.kreditunterlagen.service.UserService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public ResponseEntity<String> sayHello() {
        System.out.println("authentication");
        return ResponseEntity.ok("Hello");
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("E-Mail und Passwort müssen angegeben werden");
        }

        User user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("E-Mail oder Passwort ist falsch");
        }

        UserDetail userDetail = userService.getUserById(user.getId());
        userDetail.setPassword(null);
        return ResponseEntity.ok(userDetail);
    }

    @PostMapping("/createUser")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ein Benutzer mit dieser E-Mail existiert bereits");
        }
        User user = userService.createUser(request);
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/getUser")
    public ResponseEntity<UserDetail> getUser(@RequestParam String userId) {
        Optional<User> optionalUser = userService.findOptionalUserById(userId);
        if (optionalUser.isPresent()) {
        UserDetail userDetail = userService.getUserById(userId);
            userDetail.setPassword(null);
            return ResponseEntity.ok(userDetail);
        }

        Optional<Kreditvermittler> optionalVermittler = userService.findOptionalKreditvermittlerById(userId);
        if (optionalVermittler.isPresent()) {
            UserDetail detail = userService.convertKreditvermittlerToUserDetail(optionalVermittler.get());
            return ResponseEntity.ok(detail);
        }

    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/savePercentageUploaded")
    public ResponseEntity<Object> savePercentageUploaded(@RequestParam String userId, @RequestParam BigDecimal percentageUploaded) {
        userService.updateUploadPercentage(userId, percentageUploaded);
        return ResponseEntity.ok().body("Percentage updated successfully");
    }

    @PutMapping("/activeSecondPartner")
    public ResponseEntity<Object> activeSecondPartner(@RequestParam String userId, @RequestParam boolean activeSecondPartner) {
        userService.updateSecondPartner(userId, activeSecondPartner);
        return ResponseEntity.ok().body("Zweiter Partner erfolgreich aktualisiert");
    }

    @PutMapping("/editUser")
    public ResponseEntity<User> editUser(@RequestParam String userId, @RequestBody User updatedUserFields) {
        User updatedUser = userService.editUser(userId, updatedUserFields);
        return ResponseEntity.ok(updatedUser);
    }
}
