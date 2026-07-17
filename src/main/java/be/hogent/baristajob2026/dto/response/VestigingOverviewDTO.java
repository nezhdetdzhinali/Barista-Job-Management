package be.hogent.baristajob2026.dto.response;

public record VestigingOverviewDTO(
        Long id,
        String naam,
        String stad,
        int aantalZitplaatsen,
        long aantalActieveBaristas
) {}