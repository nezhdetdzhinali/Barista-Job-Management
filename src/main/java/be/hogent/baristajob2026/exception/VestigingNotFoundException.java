package be.hogent.baristajob2026.exception;

public class VestigingNotFoundException extends RuntimeException {
    public VestigingNotFoundException(Long id) {
        super("Vestiging not found with id %d".formatted(id));
    }
}
