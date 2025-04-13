package orgaplan.beratung.kreditunterlagen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import orgaplan.beratung.kreditunterlagen.model.Kreditvermittler;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.request.CreateNewClientRequest;
import orgaplan.beratung.kreditunterlagen.response.UserDetail;
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
        User newClient = userService.createNewClient(request);
        Map<String, Object> response = new HashMap<>();
        response.put("user", newClient);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/getUser")
    public ResponseEntity<UserDetail> getUser(@RequestParam String userId, @RequestParam boolean isKreditvermittler) {
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
