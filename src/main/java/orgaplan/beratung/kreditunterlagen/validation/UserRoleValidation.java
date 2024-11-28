package orgaplan.beratung.kreditunterlagen.validation;

import orgaplan.beratung.kreditunterlagen.Types.UserRole;

public class UserRoleValidation {

    public static boolean isValidRole(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public static void validateRole(String role) {
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Invalid user role provided: " + role);
        }
    }
}
