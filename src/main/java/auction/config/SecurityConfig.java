package auction.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(
                        (authorize) -> authorize
                                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                                .requestMatchers("/start").permitAll()
                                .requestMatchers("/users").permitAll()
                                .requestMatchers("/products").permitAll()
                                .requestMatchers("registerNewUser").permitAll()
                                .requestMatchers("/access-denied").permitAll()
                                .requestMatchers("/products/new").authenticated()
                                .requestMatchers("/noModel").permitAll()
                                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                                .requestMatchers("/webjars/**").permitAll()

                                .requestMatchers("/").permitAll()
                                .requestMatchers("/showOverallYachts").hasAuthority("ADMIN")
                                .requestMatchers("/users/delete").hasAuthority("ADMIN")
//								.requestMatchers("/user").hasAuthority("USER")
                                .anyRequest().authenticated())

                .exceptionHandling((exceptions) -> exceptions
                        .accessDeniedPage("/access-denied") // Redirect to /access-denied for denied access
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) // Custom entry point for unauthenticated users
                )

                .formLogin(form -> form //сработало, но надо html править
                        .loginPage("/login")
                        .permitAll())
                .logout((logout) -> logout
                        .logoutSuccessUrl("/")
                        .permitAll())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
    @Bean
    public JdbcUserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}