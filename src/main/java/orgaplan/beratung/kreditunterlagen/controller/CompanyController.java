package orgaplan.beratung.kreditunterlagen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import orgaplan.beratung.kreditunterlagen.model.Company;
import orgaplan.beratung.kreditunterlagen.service.CompanyService;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/getCompanyByUser")
    public @ResponseBody Company getCompanyByUser() {
        String userId = "123";
        return companyService.getCompanyByUserId(userId);
    }
}