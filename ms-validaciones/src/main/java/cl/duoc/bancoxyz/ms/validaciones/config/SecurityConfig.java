package cl.duoc.bancoxyz.ms.validaciones.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").authenticated()
                        .requestMatchers("/validaciones/**").hasRole("SERVICE")
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public PasswordEncoder codificadorClaves() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService usuariosEnMemoria(
            PasswordEncoder codificadorClaves,
            @Value("${seguridad.servicio.usuario}") String usuarioServicio,
            @Value("${seguridad.servicio.clave}") String claveServicio) {

        UserDetails servicio = User.withUsername(usuarioServicio)
                .password(codificadorClaves.encode(claveServicio))
                .roles("SERVICE")
                .build();

        return new InMemoryUserDetailsManager(servicio);
    }
}
