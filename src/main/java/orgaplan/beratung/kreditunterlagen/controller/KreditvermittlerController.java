package orgaplan.beratung.kreditunterlagen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import orgaplan.beratung.kreditunterlagen.model.Kreditvermittler;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.UserRepository;
import orgaplan.beratung.kreditunterlagen.request.KreditvermittlerForm;
import orgaplan.beratung.kreditunterlagen.response.*;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import orgaplan.beratung.kreditunterlagen.service.KreditvermittlerService;
import orgaplan.beratung.kreditunterlagen.service.UserService;

@RestController
@RequestMapping("/api/kreditvermittler")
public class KreditvermittlerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KreditvermittlerService kreditvermittlerService;

    @Autowired
    private UserService userService;

    @PostMapping("/createKreditvermittler")
    public ResponseEntity<Kreditvermittler> createKreditvermittler(@RequestBody Kreditvermittler kreditvermittler) {
        Kreditvermittler newKreditvermittler = kreditvermittlerService.createKreditvermittler(kreditvermittler);
        return ResponseEntity.status(HttpStatus.CREATED).body(newKreditvermittler);
    }

    @GetMapping("/getKreditvermittlerImageForRegistry")
    public ResponseEntity<Resource> getKreditvermittlerImageForRegistry(@RequestParam("type") String type, @RequestParam("vermittlerId") String vermittlerId) {
        try {
            Resource file = kreditvermittlerService.findImage(vermittlerId, type);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(file.getFile().toPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                    .body(file);

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getKreditvermittlerInfo")
    public ResponseEntity<KreditvermittlerInfo> getKreditvermittlerInfo(@RequestParam("vermittlerId") String vermittlerId) {
        try {
            KreditvermittlerInfo kreditvermittlerInfo = kreditvermittlerService.getKreditvermittlerInfoById(vermittlerId);
            return ResponseEntity.ok(kreditvermittlerInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/clientStatistics")
    public ResponseEntity<ClientStatisticsResponse> getClientStatistics(Principal principal) {
        String vermittlerId = principal.getName();
        ClientStatisticsResponse statistics = kreditvermittlerService.getClientStatistics(vermittlerId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/getClientsByVermittler")
    public ResponseEntity<List<ClientResponse>> getClientsByVermittler(Principal principal) {
        String vermittlerId = principal.getName();
        List<ClientResponse> clientResponses = kreditvermittlerService.getClientsByVermittlerId(vermittlerId);
        return ResponseEntity.ok(clientResponses);
    }

    @GetMapping("/getClientDetails")
    public ResponseEntity<ClientDetail> getClientDetails(Principal principal, @RequestParam String clientId) {
        String vermittlerId = principal.getName();
        try {
            ClientDetail clientDetail = kreditvermittlerService.getClientResponseByVermittlerIdAndClientId(vermittlerId, clientId);
            return ResponseEntity.ok(clientDetail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/activateForwardBanks")
    public ResponseEntity<Object> activateForwardBanks(Principal principal,
                                                       @RequestParam String userId,
                                                       @RequestParam Boolean forwardedBanks) {
        String vermittlerId = principal.getName();
        kreditvermittlerService.activateForwardBanksRequest(vermittlerId, userId, forwardedBanks);
        return ResponseEntity.ok().body("Forward Banks updated successfully");
    }

    @PutMapping("/editKreditvermittler")
    public ResponseEntity<Kreditvermittler> editKreditvermittler(Principal principal,
                                                                 @ModelAttribute KreditvermittlerForm kreditvermittlerForm) {
        String vermittlerId = principal.getName();

        try {
            Kreditvermittler updatedKreditvermittler = kreditvermittlerService.editKreditvermittler(vermittlerId, kreditvermittlerForm);
            return ResponseEntity.ok(updatedKreditvermittler);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}