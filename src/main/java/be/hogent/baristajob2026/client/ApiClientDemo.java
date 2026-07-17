package be.hogent.baristajob2026.client;

import be.hogent.baristajob2026.dto.response.BaristaOverviewDTO;
import be.hogent.baristajob2026.dto.response.BeschikbareShiftenDTO;
import be.hogent.baristajob2026.dto.response.OpleidingOverviewDTO;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ApiClientDemo {

    private final String SERVER_URI = "http://localhost:8080/api";

    private final WebClient webClient = WebClient.builder()
            .baseUrl(SERVER_URI)
            .build();

    public ApiClientDemo() {

        System.out.println("\n------- BARISTAS IN GENT -------");
        getBaristasByStad("Gent")
                .doOnNext(this::printBaristaData)
                .blockLast();

        System.out.println("\n------- BESCHIKBARE SHIFTS BARISTA 1 -------");
        getAantalBeschikbareShifts(1L)
                .doOnNext(this::printShiftData)
                .doOnError(e -> System.out.println(e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .block();

        System.out.println("\n------- OPLEIDINGEN VESTIGING 1 -------");
        getOpleidingenPerVestiging(1L)
                .doOnNext(this::printOpleidingData)
                .blockLast();

        System.out.println("\n------- ONBESTAANDE VESTIGING (verwacht 404) -------");
        getOpleidingenPerVestiging(9999L)
                .doOnNext(this::printOpleidingData)
                .doOnError(e -> System.out.println(e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .blockLast();
    }

    private Flux<BaristaOverviewDTO> getBaristasByStad(String stad) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/baristas/stad/{stad}").build(stad))
                .retrieve()
                .bodyToFlux(BaristaOverviewDTO.class);
    }

    private Mono<BeschikbareShiftenDTO> getAantalBeschikbareShifts(Long baristaId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/baristas/{id}/shifts/beschikbaar").build(baristaId))
                .retrieve()
                .bodyToMono(BeschikbareShiftenDTO.class);
    }

    private Flux<OpleidingOverviewDTO> getOpleidingenPerVestiging(Long vestigingId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/opleidingen/vestiging/{vestigingId}").build(vestigingId))
                .retrieve()
                .bodyToFlux(OpleidingOverviewDTO.class);
    }

    private void printBaristaData(BaristaOverviewDTO b) {
        System.out.printf("id=%d, %s %s, stad=%s%n", b.id(), b.voornaam(), b.achternaam(), b.stad());
    }

    private void printShiftData(BeschikbareShiftenDTO s) {
        System.out.printf("baristaId=%d, aantalBeschikbaar=%d%n", s.baristaId(), s.aantalBeschikbareShifts());
    }

    private void printOpleidingData(OpleidingOverviewDTO o) {
        System.out.printf("id=%d, titel=%s%n", o.id(), o.titel());
    }
}