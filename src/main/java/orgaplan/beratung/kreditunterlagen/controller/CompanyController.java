package orgaplan.beratung.kreditunterlagen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import orgaplan.beratung.kreditunterlagen.model.Company;
import orgaplan.beratung.kreditunterlagen.service.CompanyService;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @PreAuthorize("hasRole('firmen_kunde')")
    @GetMapping("/getCompanyByUser")
    public @ResponseBody Company getCompanyByUser(Authentication authentication) {
        String userId = authentication.getName();
        if (authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_firmen_kunde"))) {
            throw new IllegalArgumentException("Zugriff verweigert: Benutzer ist kein Firmenkunde");
        }
        return companyService.getCompanyByUserId(userId);
    }

    @PreAuthorize("hasRole('firmen_kunde')")
    @PutMapping("/editCompany")
    public @ResponseBody Company updateCompany(Authentication authentication,
                                               @RequestBody Company companyRequest) {
        String userId = authentication.getName();

        if (authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_firmen_kunde"))) {
            throw new IllegalArgumentException("Zugriff verweigert: Benutzer ist kein Firmenkunde");
        }

        return companyService.updateCompany(userId, companyRequest);
    }
}