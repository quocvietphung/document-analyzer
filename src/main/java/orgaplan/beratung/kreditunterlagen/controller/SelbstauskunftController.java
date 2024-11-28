package orgaplan.beratung.kreditunterlagen.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import orgaplan.beratung.kreditunterlagen.dto.SelbstauskunftDTO;
import orgaplan.beratung.kreditunterlagen.service.SelbstauskunftService;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/selbstauskunft")
public class SelbstauskunftController {

    private static final Logger logger = LoggerFactory.getLogger(SelbstauskunftController.class);

    @Autowired
    private SelbstauskunftService selbstauskunftService;

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @PostMapping("/createSelbstauskunftFromAI")
    public Mono<ResponseEntity<SelbstauskunftDTO>> createSelbstauskunftFromAI(Principal principal) {
        return selbstauskunftService.createSelbstauskunftFromAI(principal.getName())
                .map(selbstauskunftDTO -> ResponseEntity.ok().body(selbstauskunftDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @PostMapping("/createSelbstauskunft")
    public ResponseEntity<SelbstauskunftDTO> createSelbstauskunft(Principal principal, @Validated @RequestBody SelbstauskunftDTO request) {
        String userId = principal.getName();
        SelbstauskunftDTO savedSelbstauskunftDTO = selbstauskunftService.saveSelbstauskunft(userId, request, false);
        return ResponseEntity.ok(savedSelbstauskunftDTO);
    }

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @PutMapping("/saveTemporary")
    public ResponseEntity<SelbstauskunftDTO> saveTemporarySelbstauskunft(Principal principal, @RequestBody SelbstauskunftDTO request) {
        String userId = principal.getName();
        SelbstauskunftDTO savedSelbstauskunftDTO = selbstauskunftService.saveSelbstauskunft(userId, request, true);
        return ResponseEntity.ok(savedSelbstauskunftDTO);
    }

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @GetMapping("/getSelbstauskunftByUser")
    public ResponseEntity<SelbstauskunftDTO> getSelbstauskunftByUser(Principal principal) {
        String userId = principal.getName();
        SelbstauskunftDTO selbstauskunftDTO = selbstauskunftService.getSelbstauskunftByUser(userId);
        return ResponseEntity.ok(selbstauskunftDTO);
    }
}