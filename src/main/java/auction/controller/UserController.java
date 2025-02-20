package auction.controller;

import auction.dto.UserEmailPhotoDTO;
import auction.model.CustomUserDetails;
import auction.model.Role;
import auction.model.UserEmailImage;
import auction.repository.UserEmailRepository;
import auction.repository.UserRepository;
import auction.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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


    private final UserService userService;
    private final UserRepository userRepository;
    private final UserEmailRepository userEmailRepository;
    @Autowired
    private final PasswordEncoder encoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @GetMapping("/users")
    public String users(Model model, RedirectAttributes redirectAttributes ) {

        model.addAttribute("keyword", "test");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserUsername = authentication.getName();

        // Check if the current user is the product owner or has the ADMIN role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN") );

        if (!isAdmin ) {
            redirectAttributes.addFlashAttribute("message", "You do not have permission to users page.");
            return "redirect:/";
        }
            List<UserEmailPhotoDTO> dtoList;
            dtoList = userService.getUserEmails();
            userEmailRepository.findAll();

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
            System.out.println(customUserDetails.getRole());
            System.out.println(customUserDetails.getUsername());

            customUserDetails.setPassword(encoder.encode(password));

            System.out.println(encoder.matches(password, customUserDetails.getPassword()));

            userRepository.save(customUserDetails);
            userService.addUserAuthority(customUserDetails.getId());
            byte [] image = null ;
            if( file.getBytes().length > 0 ) {
                image = file.getBytes();
            }
            userService.addUserEmail(customUserDetails.getId(), email, image );

        } catch (Exception e) {
            LOGGER.warn("users"+e.getMessage());
        }
        return "redirect:/users";
    }


    @GetMapping("/users/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);

            redirectAttributes.addFlashAttribute("message", "The User with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/users";
    }

    @GetMapping("/users/{id}")
    public String editUser(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {

            String currentUser = getCurrentUsername();

            List<UserEmailPhotoDTO> userEmailPhotoDTOS = userService.getUserEmails();
//            UserEmailPhotoDTO dto1 = null; // альтернатива

//            for (UserEmailPhotoDTO x:userEmailPhotoDTOS) {
//                if(x.getId() == id) {
//                    dto1 = x;
//                    System.out.println("found");
//                }
//            }
            Optional<UserEmailPhotoDTO> result = userService.findUserEmailPhotoDTOById(userEmailPhotoDTOS, id);

            UserEmailPhotoDTO dto = result.get();

            System.out.println(dto.getUsername());
            System.out.println(currentUser);
            System.out.println(!dto.getUsername().equals(currentUser) && getCurrentUserAuthority().equals("[ADMIN]"));
            if (!dto.getUsername().equals(currentUser) && !getCurrentUserAuthority().equals("[ADMIN]") ) {

                redirectAttributes.addFlashAttribute("message", "You are no allowed to change user");
                return "redirect:/users";
            }

            model.addAttribute("dto", dto);

            System.out.println("edit done");

            return "editUser";
        } catch (Exception e) {
            LOGGER.warn("users exception from edit");
            return "users";
        }
    }
    private String getCurrentUserAuthority() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString(); // Gets the name of the currently authenticated user <button class="citation-flag" data-index="6">
    }
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName(); // Gets the name of the currently authenticated user <button class="citation-flag" data-index="6">
    }

    @Transactional
    @SneakyThrows
    @PostMapping("/users/saveDto")
    public String saveUser(@ModelAttribute UserEmailPhotoDTO userFormDto, Model model, @RequestParam("file") MultipartFile file) {
        // Update CustomUserDetails and UserDetailsMailPhoto separately
        System.out.println("saveDto started");
        System.out.println("userFormDto name"+ userFormDto.getUsername());
        System.out.println("userFormDto image"+ userFormDto.getUserImage().toString());

        CustomUserDetails customUserDetails = userRepository.getReferenceById(userFormDto.getId());
        customUserDetails.setUsername(userFormDto.getUsername());

        customUserDetails.setEnabled(userFormDto.isEnabled());
        customUserDetails.setRole(userFormDto.getRole());

        UserEmailImage userDetailsMailPhoto = userEmailRepository.findByUserId(customUserDetails.getId());
        userDetailsMailPhoto.setEmail(userFormDto.getEmail());

        if( file.getBytes().length > 0 ) {
            System.out.println("saveDto entered image");
            file.getBytes();
            System.out.println("file" +file.getContentType()+file.getSize()+file.getName());
            userDetailsMailPhoto.setUserImage(null);
            userDetailsMailPhoto.setUserImage(file.getBytes());
        } else {
            userDetailsMailPhoto.setUserImage(userFormDto.getUserImage());
        }

        userRepository.save(customUserDetails);

        model.addAttribute("keyword", "test");
        return "redirect:/users";
    }
    @GetMapping("/user")
    public CustomUserDetails getCurrentUser(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("No user is logged in");
        }
        System.out.println(currentUser.getUsername());
        return currentUser; // Returns the logged-in user's details
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/username")
    public String getUser(@AuthenticationPrincipal UserDetails userDetails) {
        return "username";
    }

}
