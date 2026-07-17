package be.hogent.baristajob2026.exception;

public class OpleidingNotFoundException extends RuntimeException {
    public OpleidingNotFoundException(Long id) {
        super("Opleiding niet gevonden met id %d".formatted(id));
    }
}
