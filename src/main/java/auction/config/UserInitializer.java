package auction.config;

import auction.model.CustomUserDetails;
import auction.model.Role;
import auction.repository.UserRepository;
import auction.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize adminTest user if not exists
        if (!userRepository.existsByUsername("adminTest")) {
            CustomUserDetails admin = new CustomUserDetails();
            admin.setUsername("adminTest");
            admin.setEnabled(true);
            admin.setPassword(encoder.encode("password"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            // Add email and authority for adminTest
            userService.addUserEmail(admin.getId(), "adminTest@mail.ru", null);
            userService.addUserAuthority(admin.getId());
        }

        // Initialize userTest user if not exists
        if (!userRepository.existsByUsername("userTest")) {
            CustomUserDetails user = new CustomUserDetails();
            user.setUsername("userTest");
            user.setEnabled(true);
            user.setPassword(encoder.encode("password"));
            user.setRole(Role.USER);
            userRepository.save(user);

            // Add email and authority for userTest
            userService.addUserEmail(user.getId(), "userTest@mail.ru", null);
            userService.addUserAuthority(user.getId());
        }
    }
}