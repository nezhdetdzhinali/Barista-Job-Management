package be.hogent.baristajob2026.exception;

public class ShiftNotFoundException extends RuntimeException {
    public ShiftNotFoundException(Long id) {
        super("Shift niet gevonden met id %d".formatted(id));
    }
}
