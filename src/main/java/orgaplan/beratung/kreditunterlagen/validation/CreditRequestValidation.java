package orgaplan.beratung.kreditunterlagen.validation;

import org.springframework.stereotype.Service;
import orgaplan.beratung.kreditunterlagen.Types;
import orgaplan.beratung.kreditunterlagen.model.User;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class CreditRequestValidation {

    private static final Map<Types.UserRole, Set<Types.KreditTyp>> roleToAllowedKredits = new HashMap<>();

    static {
        roleToAllowedKredits.put(Types.UserRole.PRIVAT_KUNDE, EnumSet.of(Types.KreditTyp.IMMOBILIENKREDIT, Types.KreditTyp.SONSTIGER_ANSCHAFFUNGSKREDIT));
        roleToAllowedKredits.put(Types.UserRole.FIRMEN_KUNDE, EnumSet.of(Types.KreditTyp.IMMOBILIENKREDIT, Types.KreditTyp.SCHULDSCHEINDARLEHEN));
    }

    public void validateCreditTypeForUserRole(String kreditTyp, User user) {
        Types.KreditTyp requestedKreditTyp;
        try {
            requestedKreditTyp = Types.KreditTyp.valueOf(kreditTyp);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ungültiger KreditTyp: " + kreditTyp);
        }

        if (!roleToAllowedKredits.getOrDefault(user.getRole(), EnumSet.noneOf(Types.KreditTyp.class)).contains(requestedKreditTyp)) {
            throw new IllegalArgumentException("Ungültiger KreditTyp: " + kreditTyp + " für die Rolle: " + user.getRole());
        }
    }
}