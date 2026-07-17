package be.hogent.baristajob2026.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // nodig voor JPA-ORM tool
@AllArgsConstructor // voor builder
@Builder
@EqualsAndHashCode(of = {"titel", "datum", "vestiging"})
@ToString(exclude = "id")
public class Opleiding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titel;

    @Column(nullable = false)
    private String beschrijving;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(nullable = false)
    private int duurInUur;

    @Column(nullable = false)
    private int maxDeelnemers;

    @ManyToOne
    private Vestiging vestiging;

    @ManyToMany(mappedBy = "opleidings")
    private Set<Barista> deelnemers = new HashSet<>();

}
