package be.hogent.baristajob2026.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/opleiding/*/inschrijven").hasAnyRole("BARISTA", "ADMIN")
                        .requestMatchers("/opleiding/nieuw", "/opleiding/*/bewerken").hasRole("ADMIN")
                        .requestMatchers("/vestiging/nieuw", "/vestiging/*/bewerken").hasRole("ADMIN")
                        .requestMatchers("/","/login**", "/css/**", "/403**", "/overview", "/vestiging/**","/opleiding/**","/api/**").permitAll()
                        .requestMatchers("/barista/nieuw", "/barista/*/bewerken").hasRole("ADMIN")
                        .requestMatchers("/barista/**").hasAnyRole("BARISTA", "ADMIN")
                        .requestMatchers("/shift/nieuw", "/shift/*/bewerken", "/shift/*/verwijderen").hasRole("ADMIN")
                        .anyRequest().hasAnyRole("BARISTA", "ADMIN")
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/overview", true)
                        // we werken met email ipv username volgens opgave
                        // default wordt er in de view gezocht op "username"
                        .usernameParameter("email")
                        .permitAll()
                )

                .exceptionHandling(handling -> handling
                        .accessDeniedPage("/403")
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );



        return http.build();
    }

}
