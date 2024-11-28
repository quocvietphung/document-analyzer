package orgaplan.beratung.kreditunterlagen;

import java.util.Arrays;
import java.util.List;

public class Types {
    public static final String UPLOADED_FOLDER_DOCUMENT = "uploads/documents/";
    public static final String UPLOADED_FOLDER_KREDITVERMITTLER_LOGO = "uploads/kreditvermittler/logos";
    public static final String UPLOADED_FOLDER_KREDITVERMITTLER_PROFILE = "uploads/kreditvermittler/profile";

    public enum ApplicantRole {
        ANTRAGSTELLER,
        LEBENPARTNER
    }

    public enum SelbstauskunftStatus {
        AI_GENERATED,
        TEMPORARILY_SAVED,
        COMPLETED
    }

    public enum UserRole {
        PRIVAT_KUNDE,
        FIRMEN_KUNDE,
        KREDIT_VERMITTLER,
        ;

        @Override
        public String toString() {
            if (this == PRIVAT_KUNDE) {
                return "privat_kunde";
            } else if (this == FIRMEN_KUNDE) {
                return "firmen_kunde";
            } else if (this == KREDIT_VERMITTLER) {
                return "kreditvermittler";
            }

            throw new IllegalArgumentException("Unknown user role");
        }
    }

    public enum DocumentType {
        GEHALTSABRECHNUNG,
        GEHALTSABRECHNUNG_VON_EHEPARTNER,
        AUSWEISKOPIE,
        AUSWEISKOPIE_VON_EHEPARTNER,
        KONTOAUSZUG_MIT_LETZTER_GEHALTSZAHLUNG,
        KONTOAUSZUG_MIT_LETZTER_GEHALTSZAHLUNG_VON_EHEPARTNER,
        JAHRESABSCHLUSS,
        BETRIEBSWIRTSCHAFTLICHE_AUSWERTUNG,
        UNTERLAGEN_GESCHÄFTSFÜHRER,
        SONSTIGE_DOKUMENTE,
        SELBSTAUSKUNFT,
        KONTOAUSZÜGE
    }

    public static List<String> getPrivatKundenDocuments() {
        return Arrays.asList(
                DocumentType.GEHALTSABRECHNUNG.name(),
                DocumentType.AUSWEISKOPIE.name(),
                DocumentType.KONTOAUSZUG_MIT_LETZTER_GEHALTSZAHLUNG.name()
        );
    }

    public static List<String> getEhepartnerDocuments() {
        return Arrays.asList(
                DocumentType.GEHALTSABRECHNUNG_VON_EHEPARTNER.name(),
                DocumentType.AUSWEISKOPIE_VON_EHEPARTNER.name(),
                DocumentType.KONTOAUSZUG_MIT_LETZTER_GEHALTSZAHLUNG_VON_EHEPARTNER.name()
        );
    }

    public static List<String> getFirmenKundenDocuments() {
        return Arrays.asList(
                DocumentType.JAHRESABSCHLUSS.name(),
                DocumentType.BETRIEBSWIRTSCHAFTLICHE_AUSWERTUNG.name(),
                DocumentType.UNTERLAGEN_GESCHÄFTSFÜHRER.name(),
                DocumentType.KONTOAUSZÜGE.name()
        );
    }

    public enum KreditTyp {
        IMMOBILIENKREDIT,
        SONSTIGER_ANSCHAFFUNGSKREDIT,
        SCHULDSCHEINDARLEHEN
    }
}