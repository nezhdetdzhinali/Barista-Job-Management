package be.hogent.baristajob2026.dto.request;

// voor filters op het opleidingsscherm: beschikbare/volgeboekte opleidingen, vestiging, titel
public record FilterOpleidingDTO(
        String beschikbaarheidSelected, // "beschikbaar" / "volgeboekt" / leeg = alles
        Long vestigingSelected,
        String titelSelected
) {
}
