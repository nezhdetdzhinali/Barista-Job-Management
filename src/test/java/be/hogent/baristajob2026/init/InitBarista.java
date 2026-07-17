package be.hogent.baristajob2026.init;

import java.time.LocalDate;


public class InitBarista {
    public static final Long OK_ID = 1L;
    public static final String OK_VOORNAAM = "Jan";
    public static final String OK_ACHTERNAAM = "Peeters";
    public static final String OK_EMAIL = "jan.peeters@test.be";
    // moet tussen 16 en 30 jaar liggen (@GeldigeLeeftijd op BaristaInputDTO)
    public static final LocalDate OK_GEBOORTEDATUM = LocalDate.now().minusYears(20);
    public static final String OK_STUDENTENKAARTNUMMER = "12345678";
    public static final Boolean OK_ACTIEF = true;
    public static final Long OK_VESTIGING_ID = 1L;
}
