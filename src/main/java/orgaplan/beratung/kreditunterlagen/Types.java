package orgaplan.beratung.kreditunterlagen;

import java.util.Arrays;
import java.util.List;

public class Types {

    public enum DocumentType {
        GEHALTSABRECHNUNG,
        STEUERBESCHEID,
        AUSWEISKOPIE,
        GUTHABEN_BEI_BANKEN,
        VERMOEGENSAUFSTELLUNG,
        FINANZBESCHEINIGUNG,
        UMSATZBESCHEINIGUNG
    }

    public static final List<String> PRIVAT_KUNDEN_DOCUMENTS =
            Arrays.asList(
                    DocumentType.GEHALTSABRECHNUNG.name(),
                    DocumentType.STEUERBESCHEID.name(),
                    DocumentType.AUSWEISKOPIE.name(),
                    DocumentType.GUTHABEN_BEI_BANKEN.name(),
                    DocumentType.VERMOEGENSAUFSTELLUNG.name()
            );

    public static final List<String> FIRMEN_KUNDEN_DOCUMENTS =
            Arrays.asList(
                    DocumentType.STEUERBESCHEID.name(),
                    DocumentType.FINANZBESCHEINIGUNG.name(),
                    DocumentType.AUSWEISKOPIE.name(),
                    DocumentType.UMSATZBESCHEINIGUNG.name()
            );
}
