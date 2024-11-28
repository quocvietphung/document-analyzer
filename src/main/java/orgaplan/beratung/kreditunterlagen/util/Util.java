package orgaplan.beratung.kreditunterlagen.util;

import java.security.SecureRandom;
import java.util.Random;

public class Util {

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public static String changeExtensionToPdf(String originalFilename) {
        if (originalFilename != null) {
            int indexOfLastDot = originalFilename.lastIndexOf('.');
            if (indexOfLastDot != -1) {
                String filenameWithoutExtension = originalFilename.substring(0, indexOfLastDot);
                return filenameWithoutExtension + ".pdf";
            } else {
                return originalFilename + ".pdf";
            }
        }
        return null;
    }

    public static String generateStartPassword() {
        int length = 9;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }

        return password.toString();
    }
}
