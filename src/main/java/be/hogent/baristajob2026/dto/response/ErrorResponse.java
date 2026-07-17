package be.hogent.baristajob2026.dto.response;

public record ErrorResponse(
        int status,
        String message,
        String timestamp
) {
}
