package be.hogent.baristajob2026.exception;

public class BaristaNotFoundException extends RuntimeException {
    public BaristaNotFoundException(Long id) {
        super("Barista niet gevonden met id %d".formatted(id));
    }

    public BaristaNotFoundException(String email) {
        super("Barista niet gevonden met email %s".formatted(email));
    }
}
