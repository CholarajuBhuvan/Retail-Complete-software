package in.bhuvan.billingsoftware.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")
                )
                .cors(cors -> {}) // enable CORS handling via CorsFilter
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/h2-console/**", "/api/health").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/me").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("ADMIN", "MANAGER", "CASHIER", "EMPLOYEE")
                        .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                        .requestMatchers("/api/receipts/**").hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                        .requestMatchers("/api/reports/**", "/api/dashboard/**").hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                        .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/customers/**").hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                        .requestMatchers("/api/users/**", "/api/suppliers/**", "/api/categories/**", "/api/discounts/**", "/api/payments/**", "/api/shifts/**", "/api/barcodes/**").hasAnyRole("ADMIN", "MANAGER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .httpBasic(basic -> basic.disable())
                .headers(h -> h.frameOptions(f -> f.disable())); // allow H2 console
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


