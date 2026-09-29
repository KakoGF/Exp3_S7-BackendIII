package cl.duoc.bancoxyz.bff.cajero.config;

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
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").authenticated()
                        .requestMatchers("/bff/cajero/**").hasRole("CAJERO")
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
            @Value("${seguridad.canal.usuario}") String usuarioCanal,
            @Value("${seguridad.canal.clave}") String claveCanal,
            @Value("${seguridad.invitado.usuario}") String usuarioInvitado,
            @Value("${seguridad.invitado.clave}") String claveInvitado) {

        UserDetails canal = User.withUsername(usuarioCanal)
                .password(codificadorClaves.encode(claveCanal))
                .roles("CAJERO")
                .build();

        UserDetails invitado = User.withUsername(usuarioInvitado)
                .password(codificadorClaves.encode(claveInvitado))
                .roles("INVITADO")
                .build();

        return new InMemoryUserDetailsManager(canal, invitado);
    }
}
