package be.hogent.baristajob2026.service;

import be.hogent.baristajob2026.model.Barista;
import be.hogent.baristajob2026.model.Rol;
import be.hogent.baristajob2026.repository.BaristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class BaristaUserDetailsService implements UserDetailsService {
    private final BaristaRepository baristaRepository;

    // wordt aangeroepn door spring security tijdens het inlog process
    // we moeten alleen de data inleveren
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Barista barista = baristaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Barista not found: " + email));

        // Geef een Spring Security User-object terug met:
        // - de email uit de database
        // - het gehashte wachtwoord uit de database (Spring vergelijkt dit met het ingetypte wachtwoord)
        // - de rollen/rechten van de gebruiker (omgezet via convertAuthorities)
        return new User(
                barista.getEmail(),
                barista.getPassword(),
                convertAuthorities(barista.getRol())
        );
    }

    private Collection<? extends GrantedAuthority> convertAuthorities(Rol role) {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_%s".formatted(role.name())));
    }
}