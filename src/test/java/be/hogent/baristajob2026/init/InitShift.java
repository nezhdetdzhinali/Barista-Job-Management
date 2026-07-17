package be.hogent.baristajob2026.init;

import be.hogent.baristajob2026.model.ShiftRol;

import java.time.LocalDate;
import java.time.LocalTime;

public class InitShift {
    public static final Long OK_ID = 1L;
    public static final LocalDate OK_DATUM = LocalDate.now().plusDays(7);
    public static final LocalTime OK_START_UUR = LocalTime.of(9, 0);
    // 4 uur duur: valt binnen de @GeldigeShiftTijden grenzen (min 2u, max 8u)
    public static final LocalTime OK_EIND_UUR = LocalTime.of(13, 0);
    public static final ShiftRol OK_ROL = ShiftRol.BARISTA;
    public static final Integer OK_MAX_BARISTAS = 3;
    public static final Long OK_VESTIGING_ID = 1L;
}
