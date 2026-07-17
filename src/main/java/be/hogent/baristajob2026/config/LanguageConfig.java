package be.hogent.baristajob2026.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

// de resource bundle zelf wordt geconfigureerd via spring.messages.basename in application.properties
// ipv hier nog eens een aparte ResourceBundleMessageSource bean aan te maken
@Configuration
public class LanguageConfig {

    @Bean
    LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        // nl is de standaardtaal van het project in de browser
        slr.setDefaultLocale(new Locale("nl"));
        return slr;
    }

}