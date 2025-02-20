package auction.service;

import auction.dto.UserEmailPhotoDTO;
import auction.model.*;
import auction.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserEmailRepository userEmailRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BidRepository bidRepository;

    @Transactional
    public void addUserEmail(Long userId, String email, byte[] userImage) {
        CustomUserDetails user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (userEmailRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }
        UserEmailImage userEmailImage = UserEmailImage.builder()
                .user(user)
                .email(email)
                .userImage(userImage)
                .build();
        userEmailRepository.save(userEmailImage);
    }

    @Transactional
    public void addUserAuthority(Long userId) {

        CustomUserDetails user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Authority authority = Authority.builder()
                .username(user.getUsername())
                .authority(user.getRole().getAuthority())
                .user(user)
                .build();

        authorityRepository.save(authority);
    }

    @Transactional
    public List<UserEmailPhotoDTO> getUserEmails() {
        return userRepository.findAll().stream()
                .map(user -> {
                    CustomUserDetails userDetails = user;
                    UserEmailImage userEmailImage = userEmailRepository.findByUserId(user.getId());
                    if (userEmailImage != null) {
                        return new UserEmailPhotoDTO(userDetails.getId(), userDetails.getUsername(), userEmailImage.getEmail(), userDetails.getRole(), userDetails.isEnabled(), userEmailImage.getUserImage());
                    } else {
                        return new UserEmailPhotoDTO(userDetails.getId(), userDetails.getUsername(), userEmailImage.getEmail(), userDetails.getRole(), userDetails.isEnabled(), userEmailImage.getUserImage()); // Handle case where no email exists
                    }
                })
                .collect(Collectors.toList());
    }

    public void deleteUser(Long userId) {

        userRepository.deleteById(userId); // This will also delete associated UserMail entries
        // Удалить пользователя из таблицы users
    }

    @Transactional
    public void updateUserUsername(Long userId, String newUsername) {
        CustomUserDetails user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the username
        user.setUsername(newUsername);
        // Save the updated user (this will also save associated authorities)
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long userId, String newUsername, String Role) {
        CustomUserDetails user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the username
        user.setUsername(newUsername);
        user.setRole(auction.model.Role.valueOf(Role));
        user.onUpdate();

        // Save the updated user (this will also save associated authorities)
        userRepository.save(user);
    }

    public Optional<UserEmailPhotoDTO> findUserEmailPhotoDTOById(List<UserEmailPhotoDTO> dtoList, Long id) {
        return dtoList.stream()
                .filter(dto -> dto.getId().equals(id)) // Filter by ID
                .findFirst(); // Return the first matching DTO
    }

    @GetMapping("/users/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            userRepository.deleteById(id);

            redirectAttributes.addFlashAttribute("message", "The user with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/users";
    }

    @Transactional
    public void placeBid(Long userId, Long lotId, BigDecimal amount) {


        CustomUserDetails user = userRepository.getReferenceById(userId);
        Product product = productRepository.getReferenceById(lotId);

        Bid bid = new Bid();
        bid.setAmount(amount);
        bid.setUser(user);
        bid.setProduct(product);

        bidRepository.save(bid);
        product.setCurrentPrice(amount);
        productRepository.save(product);
    }
}