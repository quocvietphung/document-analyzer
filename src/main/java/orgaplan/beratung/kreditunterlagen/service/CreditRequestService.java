package orgaplan.beratung.kreditunterlagen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import orgaplan.beratung.kreditunterlagen.Types;
import orgaplan.beratung.kreditunterlagen.model.CreditRequest;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.CreditRequestRepository;
import orgaplan.beratung.kreditunterlagen.request.CreditRequestForm;
import orgaplan.beratung.kreditunterlagen.response.CreditRequestResponse;
import orgaplan.beratung.kreditunterlagen.validation.CreditRequestValidation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditRequestService {

    @Autowired
    private UserService userService;

    @Autowired
    private CreditRequestRepository creditRequestRepository;

    @Autowired
    private CreditRequestValidation creditRequestValidation;

    public CreditRequestResponse saveCreditRequest(String userId, CreditRequestForm creditRequestForm) throws Exception {
        User user = userService.findUserById(userId);

        if (user.getForwardedBanks()) {
            throw new RuntimeException("Die Weiterleitung zu den Banken wurde aktiviert, daher können Sie diese Kreditanfrage nicht speichern.");
        }

        creditRequestValidation.validateCreditTypeForUserRole(creditRequestForm.getKreditTyp(), user);
        Types.KreditTyp docType = Types.KreditTyp.valueOf(creditRequestForm.getKreditTyp());

        CreditRequest creditRequest = new CreditRequest();
        creditRequest.setUser(user);
        creditRequest.setKreditTyp(docType);
        creditRequest.setKreditLink(creditRequestForm.getKreditLink());
        creditRequest.setBetrag(creditRequestForm.getBetrag());
        creditRequest.setLaufzeit(creditRequestForm.getLaufzeit());
        creditRequest.setCreatedAt(LocalDateTime.now());
        creditRequest.setUpdatedAt(LocalDateTime.now());

        creditRequest = creditRequestRepository.save(creditRequest);

        return toResponse(creditRequest);
    }

    public CreditRequestResponse updateCreditRequest(String userId, String creditRequestId, CreditRequestForm creditRequestForm)  {
        User user = userService.findUserById(userId);

        if (user.getForwardedBanks()) {
            throw new RuntimeException("Die Weiterleitung zu den Banken wurde aktiviert, daher können Sie diese Kreditanfrage nicht aktualisieren.");
        }

        CreditRequest creditRequest = creditRequestRepository.findByUserAndId(user, creditRequestId);
        if (creditRequest == null) {
            throw new IllegalArgumentException("Credit Request not found for the given user and ID.");
        }

        creditRequestValidation.validateCreditTypeForUserRole(creditRequestForm.getKreditTyp(), user);
        Types.KreditTyp docType = Types.KreditTyp.valueOf(creditRequestForm.getKreditTyp());

        creditRequest.setKreditTyp(docType);
        creditRequest.setKreditLink(creditRequestForm.getKreditLink());
        creditRequest.setBetrag(creditRequestForm.getBetrag());
        creditRequest.setLaufzeit(creditRequestForm.getLaufzeit());
        creditRequest.setUpdatedAt(LocalDateTime.now());

        creditRequest = creditRequestRepository.save(creditRequest);

        return toResponse(creditRequest);
    }

    public List<CreditRequestResponse> getCreditRequestsByUser(String userId) {
        User user = userService.findUserById(userId);

        List<CreditRequest> requests = creditRequestRepository.findByUserOrderByCreatedAtDesc(user);
        return requests.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private CreditRequestResponse toResponse(CreditRequest creditRequest) {
        return CreditRequestResponse.builder()
                .id(creditRequest.getId())
                .kreditTyp(creditRequest.getKreditTyp())
                .kreditLink(creditRequest.getKreditLink())
                .betrag(creditRequest.getBetrag())
                .laufzeit(creditRequest.getLaufzeit())
                .build();
    }

    public CreditRequestResponse getCreditRequest(String userId, String creditRequestId) {
        User user = userService.findUserById(userId);

        CreditRequest creditRequest = creditRequestRepository.findByUserAndId(user, creditRequestId);
        if (creditRequest == null) {
            throw new IllegalArgumentException("Credit Request not found for the given user and ID.");
        }

        return toResponse(creditRequest);
    }

    public void deleteCreditRequestByIdAndUser(String userId, String creditRequestId) {
        User user = userService.findUserById(userId);

        CreditRequest creditRequest = creditRequestRepository.findByUserAndId(user, creditRequestId);
        if (creditRequest == null) {
            throw new IllegalArgumentException("Credit Request not found for the given user and ID.");
        }

        if (user.getForwardedBanks()) {
            throw new RuntimeException("Die Weiterleitung zu den Banken wurde aktiviert, daher können Sie diese Kreditanfrage nicht löschen.");
        }

        // Delete the record from the database
        creditRequestRepository.deleteById(creditRequestId);
    }
}