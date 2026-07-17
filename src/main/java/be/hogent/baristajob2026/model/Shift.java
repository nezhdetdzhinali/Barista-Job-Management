package be.hogent.baristajob2026.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter // voor shift form
@NoArgsConstructor(access = AccessLevel.PROTECTED) // nodig voor JPA-ORM tool
@AllArgsConstructor // voor builder
@Builder
@EqualsAndHashCode(of = {"datum",
        "startUur",
        "eindUur",
        "vestiging"})
@ToString(exclude = "id")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(nullable = false)
    private LocalTime startUur;

    @Column(nullable = false)
    private LocalTime eindUur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftRol rol;

    @Column(nullable = false)
    private int maxBaristas;

    @ManyToOne
    private Vestiging vestiging;

    @ManyToMany(mappedBy = "shifts")
    private Set<Barista> ingeschrevenBaristas = new HashSet<>();

}
