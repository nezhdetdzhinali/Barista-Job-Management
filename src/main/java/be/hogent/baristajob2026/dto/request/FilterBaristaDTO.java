package be.hogent.baristajob2026.dto.request;

public record FilterBaristaDTO(
        String stadSelected,
        String actiefSelected,
        // opleidings id
        Long opleidingSelected,
        String sortSelected) {
}
