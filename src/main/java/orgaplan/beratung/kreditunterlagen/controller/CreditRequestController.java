package orgaplan.beratung.kreditunterlagen.controller;

import jakarta.validation.groups.Default;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import orgaplan.beratung.kreditunterlagen.request.CreditRequestForm;
import orgaplan.beratung.kreditunterlagen.response.CreditRequestResponse;
import orgaplan.beratung.kreditunterlagen.service.CreditRequestService;
import orgaplan.beratung.kreditunterlagen.service.KreditvermittlerService;
import orgaplan.beratung.kreditunterlagen.validation.ValidationGroups;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/credit-requests")
public class CreditRequestController {

    private static final Logger logger = LoggerFactory.getLogger(CreditRequestController.class);

    @Autowired
    private CreditRequestService creditRequestService;

    @Autowired
    private KreditvermittlerService kreditvermittlerService;

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @PostMapping("/send")
    public CreditRequestResponse saveCreditRequest(Principal principal,
                                                   @Validated({ValidationGroups.Create.class, Default.class})
                                                   @RequestBody CreditRequestForm creditRequestForm) throws Exception {
        String userId = principal.getName();
        return creditRequestService.saveCreditRequest(userId, creditRequestForm);
    }

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @PutMapping("/editCreditRequest")
    public CreditRequestResponse editCreditRequest(Principal principal,
                                                   @Validated({ValidationGroups.Update.class, Default.class})
                                                   @RequestBody CreditRequestForm creditRequestForm) throws Exception {
        String userId = principal.getName();
        return creditRequestService.updateCreditRequest(userId, creditRequestForm.getId(), creditRequestForm);
    }

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @GetMapping("/getCreditRequestsByUser")
    public ResponseEntity<?> getCreditRequestsByUser(Authentication authentication){
        String userId = authentication.getName();

        List<CreditRequestResponse> responseList = creditRequestService.getCreditRequestsByUser(userId);

        return ResponseEntity.ok(responseList);
    }

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @DeleteMapping("/deleteCreditRequest")
    public ResponseEntity<?> deleteCreditRequestByIdAndUser(Principal principal, @RequestParam String creditRequestId) {
        String userId = principal.getName();
        creditRequestService.deleteCreditRequestByIdAndUser(userId, creditRequestId);
        return ResponseEntity.ok("Credit Request Deleted Successfully");
    }
}