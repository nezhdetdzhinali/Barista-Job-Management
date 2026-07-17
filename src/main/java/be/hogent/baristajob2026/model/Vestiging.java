package be.hogent.baristajob2026.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // nodig voor JPA-ORM tool
@AllArgsConstructor // voor builder
@Builder
@EqualsAndHashCode(of = {"naam", "stad"})
@ToString(exclude = "id")
public class Vestiging {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) //PK is door db ingevuld + auto increment
    private Long id;

    @Column(nullable = false)
    private String naam;

    @Column(nullable = false)
    private String stad;

    @Column(nullable = false)
    private int aantalZitplaatsen;

    @OneToMany(mappedBy = "vestiging") // owning side is Barista, mappedBy komt dus in de inverse side (Vestiging)
    private List<Barista> baristas = new ArrayList<>();

    @OneToMany(mappedBy = "vestiging") // zelfde redeneing als hierboven
    private List<Shift> shifts = new ArrayList<>();

    @OneToMany(mappedBy = "vestiging")
    private List<Opleiding> opleidingen = new ArrayList<>();

    public Vestiging(String naam,
                     String stad,
                     int aantalZitplaatsen) {
        this.naam = naam;
        this.stad = stad;
        this.aantalZitplaatsen = aantalZitplaatsen;
    }

}