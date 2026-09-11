package com.library.config;

import com.library.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   // <-- enable method-level security
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(authz -> authz
                        // ---------- PUBLIC ----------
                        .requestMatchers("/", "/home", "/error", "/error/**").permitAll()
                        .requestMatchers("/books", "/books/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/register", "/login", "/logout", "/perform_login").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // ---------- AI Assistant — authenticated users ----------
                        .requestMatchers("/api/ai/**").authenticated()

                        // ---------- Librarian/Admin — book management ----------
                        .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**")
                        .hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers("/api/books/manage/**")
                        .hasAnyRole("LIBRARIAN", "ADMIN")

                        // ---------- STAFF ONLY — loan management dashboard ----------
                        .requestMatchers("/loans/manage", "/loans/manage/**")
                        .hasAnyRole("LIBRARIAN", "ADMIN")

                        // ---------- STAFF ONLY — return books ----------
                        .requestMatchers("/loans/return/**")
                        .hasAnyRole("LIBRARIAN", "ADMIN")

                        // ---------- Admin only ----------
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ---------- Member only — borrowing ----------
                        .requestMatchers("/books/borrow/**").hasRole("MEMBER")

                        // ---------- Cart — authenticated users ----------
                        .requestMatchers("/cart/**").authenticated()

                        // ---------- My Loans view — any authenticated user ----------
                        .requestMatchers("/loans", "/loans/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error/403")
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}