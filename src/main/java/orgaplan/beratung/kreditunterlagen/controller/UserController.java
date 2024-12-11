package orgaplan.beratung.kreditunterlagen.controller;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import orgaplan.beratung.kreditunterlagen.model.Kreditvermittler;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.request.CreateNewClientRequest;
import orgaplan.beratung.kreditunterlagen.response.UserDetail;
import orgaplan.beratung.kreditunterlagen.service.EmailService;
import orgaplan.beratung.kreditunterlagen.service.KreditvermittlerService;
import orgaplan.beratung.kreditunterlagen.service.UserService;
import orgaplan.beratung.kreditunterlagen.util.Util;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private KreditvermittlerService kreditvermittlerService;

    @GetMapping("/test")
    public ResponseEntity<String> sayHello() {
        System.out.println("authentication");
        return ResponseEntity.ok("Hello");
    }

    @PostMapping("/createNewClient")
    public ResponseEntity<Map<String, Object>> createNewClient(@RequestBody CreateNewClientRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ein Benutzer mit dieser E-Mail existiert bereits");
        }

        String startPassword = Util.generateStartPassword();
        User newClient = userService.createNewClient(request);
        Map<String, Object> response = new HashMap<>();
        response.put("user", newClient);

        // 3. Step: Mail with "Nutzername" = E-Mail and "Startpasswort"
        String email = request.getEmail();
        try {
            emailService.sendWelcomeEmail(email,startPassword);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to send email."));
        }
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/getUser")
    public ResponseEntity<UserDetail> getUser() {
        boolean isKreditvermittler = false;
        String userId = "e33449f9-e4fb-4c06-a1fb-3ebf1e426bac";

        UserDetail userDetail;
        if (isKreditvermittler) {
            Kreditvermittler kreditvermittler = userService.findKreditvermittlerById(userId);
            userDetail = userService.convertKreditvermittlerToUserDetail(kreditvermittler);
        } else {
            userDetail = userService.getUserById(userId);
        }

        return ResponseEntity.ok(userDetail);
    }

    @PutMapping("/savePercentageUploaded")
    public ResponseEntity<Object> savePercentageUploaded(Principal principal, @RequestParam BigDecimal percentageUploaded) {
        String userId = "e33449f9-e4fb-4c06-a1fb-3ebf1e426bac";
        userService.updateUploadPercentage(userId, percentageUploaded);
        return ResponseEntity.ok().body("Percentage updated successfully");
    }

    @PutMapping("/activeSecondPartner")
    public ResponseEntity<Object> activeSecondPartner(Principal principal, @RequestParam boolean activeSecondPartner) {
        String userId = "e33449f9-e4fb-4c06-a1fb-3ebf1e426bac";
        userService.updateSecondPartner(userId, activeSecondPartner);
        return ResponseEntity.ok().body("Zweiter Partner erfolgreich aktualisiert");
    }

    @PutMapping("/editUser")
    public ResponseEntity<User> editUser(@RequestBody User updatedUserFields) {
        String userId = "e33449f9-e4fb-4c06-a1fb-3ebf1e426bac";
        User updatedUser = userService.editUser(userId, updatedUserFields);
        return ResponseEntity.ok(updatedUser);
    }
}
