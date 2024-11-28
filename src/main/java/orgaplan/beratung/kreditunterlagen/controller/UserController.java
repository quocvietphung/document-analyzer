package orgaplan.beratung.kreditunterlagen.controller;

import jakarta.mail.MessagingException;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private KreditvermittlerService kreditvermittlerService;

    @PreAuthorize("hasRole('kreditvermittler') || hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @GetMapping("/test")
    public ResponseEntity<String> sayHello(Authentication authentication, Principal principal) {
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            Jwt jwt = (Jwt) jwtToken.getPrincipal();
            String accessToken = jwt.getTokenValue();

            System.out.println("Access Token: " + accessToken);
        }

        System.out.println("Principal Name: " + principal.getName());
        System.out.println("Authentication Name: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());

        String roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(", "));

        String message = "Hello, World! Your roles are: " + roles;
        return ResponseEntity.ok(message);
    }

    @PostMapping("/createNewClient")
    public ResponseEntity<Map<String, Object>> createNewClient(@RequestBody CreateNewClientRequest request) {
        // 1. Step: Generate start password with length of 9
        // 2. Step: Make API request to get new User UUID; Request: Mail, Start-Password -> Response: UUID from user;
        /*
            API REQUEST:
            Type: POST
            URL: http://127.0.0.1:8500/user/create
            RESPONSE:
                CODE: 200
                BODY: STRING (UUID from new user)
            REQUEST:
                BODY:
                    firstname: STRING
                    lastname: STRING
                    password: STRING
                    mail: STRING
                    role: STRING ("privat_kunde" OR "firmen_kunde")
         */

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

    @PreAuthorize("hasRole('kreditvermittler')")
    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @PreAuthorize("hasRole('kreditvermittler') || hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @GetMapping("/getUser")
    public ResponseEntity<UserDetail> getUserById(Authentication authentication) {
        boolean isKreditvermittler = kreditvermittlerService.isKreditvermittler(authentication);
        String userId = authentication.getName();

        UserDetail userDetail;
        if (isKreditvermittler) {
            Kreditvermittler kreditvermittler = userService.findKreditvermittlerById(userId);
            userDetail = userService.convertKreditvermittlerToUserDetail(kreditvermittler);
        } else {
            userDetail = userService.getUserById(userId);
        }

        return ResponseEntity.ok(userDetail);
    }

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @PutMapping("/savePercentageUploaded")
    public ResponseEntity<Object> savePercentageUploaded(Principal principal, @RequestParam BigDecimal percentageUploaded) {
        String userId = principal.getName();
        userService.updateUploadPercentage(userId, percentageUploaded);
        return ResponseEntity.ok().body("Percentage updated successfully");
    }

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @PutMapping("/activeSecondPartner")
    public ResponseEntity<Object> activeSecondPartner(Principal principal, @RequestParam boolean activeSecondPartner) {
        String userId = principal.getName();
        userService.updateSecondPartner(userId, activeSecondPartner);
        return ResponseEntity.ok().body("Zweiter Partner erfolgreich aktualisiert");
    }

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @PutMapping("/editUser")
    public ResponseEntity<User> editUser(Authentication authentication,
                                         @RequestBody User updatedUserFields) {
        String userId = authentication.getName();
        User updatedUser = userService.editUser(userId, updatedUserFields);
        return ResponseEntity.ok(updatedUser);
    }
}
