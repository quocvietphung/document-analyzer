package orgaplan.beratung.kreditunterlagen.controller;

import jakarta.validation.groups.Default;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import orgaplan.beratung.kreditunterlagen.request.CreditRequestForm;
import orgaplan.beratung.kreditunterlagen.response.CreditRequestResponse;
import orgaplan.beratung.kreditunterlagen.service.CreditRequestService;
import orgaplan.beratung.kreditunterlagen.validation.ValidationGroups;

import java.util.List;

@RestController
@RequestMapping("/api/credit-requests")
public class CreditRequestController {

    private static final Logger logger = LoggerFactory.getLogger(CreditRequestController.class);

    @Autowired
    private CreditRequestService creditRequestService;

    @Autowired
    private KreditvermittlerService kreditvermittlerService;

    @PostMapping("/send")
    public CreditRequestResponse saveCreditRequest(
            @Validated({ValidationGroups.Create.class, Default.class})
            @RequestBody CreditRequestForm creditRequestForm,
            @RequestParam String userId) throws Exception {
        return creditRequestService.saveCreditRequest(userId, creditRequestForm);
    }

    @PutMapping("/editCreditRequest")
    public CreditRequestResponse editCreditRequest(
            @Validated({ValidationGroups.Update.class, Default.class})
            @RequestBody CreditRequestForm creditRequestForm,
            @RequestParam String userId)  {
        return creditRequestService.updateCreditRequest(userId, creditRequestForm.getId(), creditRequestForm);
    }

    @GetMapping("/getCreditRequestsByUser")
    public ResponseEntity<?> getCreditRequestsByUser(@RequestParam String userId){
        List<CreditRequestResponse> responseList = creditRequestService.getCreditRequestsByUser(userId);
        return ResponseEntity.ok(responseList);
    }

    @DeleteMapping("/deleteCreditRequest")
    public ResponseEntity<?> deleteCreditRequestByIdAndUser(@RequestParam String creditRequestId, @RequestParam String userId) {
        creditRequestService.deleteCreditRequestByIdAndUser(userId, creditRequestId);
        return ResponseEntity.ok("Credit Request Deleted Successfully");
    }
}