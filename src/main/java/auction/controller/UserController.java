package auction.controller;

import auction.dto.UserEmailPhotoDTO;
import auction.exception.AccessDeniedException;
import auction.exception.UserNotFoundException;
import auction.model.CustomUserDetails;
import auction.model.Role;
import auction.repository.UserEmailRepository;
import auction.repository.UserRepository;
import auction.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class UserController {

    private static final Logger LOGGER = LogManager.getLogger(UserController.class);
    @Autowired
    private final UserService userService;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserEmailRepository userEmailRepository;
    @Autowired
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @GetMapping("/users")
    public String users(Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("keyword", "test");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("You must be logged in to access the users page.");
        }

        String currentUserUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("You do not have permission to access the users page.");
        }

        List<UserEmailPhotoDTO> dtoList = userService.getUserEmails();
        model.addAttribute("dtoList", dtoList);
        return "users";
    }

    @GetMapping("/registerNewUser")
    public String registerUser(Model model, @Param("email") String email, @Param("password") String password) {
        CustomUserDetails customUserDetails = new CustomUserDetails();
        customUserDetails.setRole(Role.USER);
        customUserDetails.setEnabled(true);

        model.addAttribute("customUserDetails", customUserDetails);
        model.addAttribute("email", email);
        model.addAttribute("password", password);

        return "registerNewUser";
    }

    @PostMapping("/users/save")
    public String saveUser(CustomUserDetails customUserDetails, @RequestParam("email") String email, @RequestParam("password") String password, @RequestParam("file") MultipartFile file) {
        try {
            customUserDetails.setPassword(encoder.encode(password));
            userRepository.save(customUserDetails);
            userService.addUserAuthority(customUserDetails.getId());

            byte[] image = null;
            if (file.getBytes().length > 0) {
                image = file.getBytes();
            }
            userService.addUserEmail(customUserDetails.getId(), email, image);
        } catch (Exception e) {
            LOGGER.warn("Error saving user: {}", e.getMessage());
            throw new RuntimeException("Error saving user: " + e.getMessage(), e); // Let the global handler manage this
        }
        return "redirect:/users";
    }

    @DeleteMapping("/users/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "The User with ID=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            LOGGER.warn("Error deleting user: {}", e.getMessage());
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e); // Let the global handler manage this
        }
        return "redirect:/users";
    }

    @GetMapping("/users/{id}")
    public String editUser(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            String currentUser = getCurrentUsername();
            List<UserEmailPhotoDTO> userEmailPhotoDTOS = userService.getUserEmails();
            Optional<UserEmailPhotoDTO> result = userService.findUserEmailPhotoDTOById(userEmailPhotoDTOS, id);

            if (result.isEmpty()) {
                throw new UserNotFoundException("User with ID=" + id + " not found");
            }

            UserEmailPhotoDTO dto = result.get();

            if (!dto.getUsername().equals(currentUser) && !getCurrentUserAuthority().equals("[ADMIN]")) {
                throw new AccessDeniedException("You are not allowed to edit this user.");
            }

            model.addAttribute("dto", dto);
            return "editUser";
        } catch (Exception e) {
            LOGGER.warn("Error editing user: {}", e.getMessage());
            throw e; // Let the global handler manage this
        }
    }

    private String getCurrentUserAuthority() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Transactional
    @PostMapping("/users/saveDto")
    public String saveUser(@ModelAttribute UserEmailPhotoDTO userFormDto, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            userService.saveUserWithImage(userFormDto, file);
        } catch (UserNotFoundException | AccessDeniedException e) {
            LOGGER.warn("Error updating user: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/users";
        } catch (Exception e) {
            LOGGER.error("Unexpected error: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("message", "Error: " + e.getMessage());
            return "redirect:/users";
        }
        redirectAttributes.addFlashAttribute("message", "The user has been updated successfully!");
        return "redirect:/users";
    }

    @GetMapping("/user")
    public CustomUserDetails getCurrentUser(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("No user is logged in");
        }
        System.out.println(currentUser.getUsername());
        userService.getUserEmails();
        return currentUser;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }


}