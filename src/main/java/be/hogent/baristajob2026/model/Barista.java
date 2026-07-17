package be.hogent.baristajob2026.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter // setter nodig voor barista form
@NoArgsConstructor(access = AccessLevel.PROTECTED) // nodig voor JPA-ORM tool
@AllArgsConstructor // voor builder
@Builder
@EqualsAndHashCode(of = {"email", "studentenkaartNummer"})
@ToString(exclude = "id")
public class Barista {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK is door db ingevuld + auto increment
    private Long id;

    @Column(nullable = false, unique = true)
    private String studentenkaartNummer;

    @Column(nullable = false)
    private String voornaam;

    @Column(nullable = false)
    private String achternaam;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;


    @Column(nullable = false)
    private LocalDate geboortedatum;

    @Column(nullable = false)
    private boolean actief;

    @Column(nullable = false)
    private boolean administratiefGeblokkeerd;

    // Barista is de owning side van de relaties, mapping by staat dus in de inverse sides
    @ManyToOne
    private Vestiging vestiging;

    @ManyToMany
    private Set<Shift> shifts = new HashSet<>();

    @ManyToMany
    private Set<Opleiding> opleidings = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ShiftRol shiftRol;

    // security rolen
    @Enumerated(EnumType.STRING)
    private Rol rol;
}