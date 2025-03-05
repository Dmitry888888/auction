package auction.service;

import auction.dto.UserEmailPhotoDTO;
import auction.model.Authority;
import auction.model.CustomUserDetails;
import auction.model.UserEmailImage;
import auction.repository.*;
import lombok.SneakyThrows;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.logging.log4j.LogManager;

import org.springframework.web.multipart.MultipartFile;
import auction.exception.AccessDeniedException;
import auction.exception.UserNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LogManager.getLogger(UserService.class);
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


    public Optional<UserEmailPhotoDTO> findUserEmailPhotoDTOById(List<UserEmailPhotoDTO> dtoList, Long id) {
        return dtoList.stream()
                .filter(dto -> dto.getId().equals(id)) // Filter by ID
                .findFirst(); // Return the first matching DTO
    }


    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // No user is logged in
        }
        return authentication.getName(); // Returns the username
    }

    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username == null) {
            return null; // No user is logged in
        }
        return userRepository.findByUsername(username).getId(); // Fetch the user ID from the repository
    }

    @SneakyThrows
    @Transactional
    public void saveUserWithImage(UserEmailPhotoDTO userFormDto, MultipartFile file)
            throws UserNotFoundException, AccessDeniedException {
        try {
            // Шаг 1: Проверяем существование CustomUserDetails
            Optional<CustomUserDetails> optionalUserDetails = userRepository.findById(userFormDto.getId());
            if (optionalUserDetails.isEmpty()) {
                throw new UserNotFoundException("CustomUserDetails with ID=" + userFormDto.getId() + " not found");
            }

            CustomUserDetails customUserDetails = optionalUserDetails.get();

            // Шаг 2: Проверяем права доступа
            checkPermission(customUserDetails);

            // Шаг 3: Обновляем данные CustomUserDetails
            customUserDetails.setUsername(userFormDto.getUsername());
            customUserDetails.setEnabled(userFormDto.isEnabled());
            customUserDetails.setRole(userFormDto.getRole());

            // Шаг 4: Ищем UserEmailImage по ID пользователя
            Optional<UserEmailImage> optionalUserEmailImage = Optional.ofNullable(userEmailRepository.findByUserId(customUserDetails.getId()));
            if (optionalUserEmailImage.isEmpty()) {
                throw new UserNotFoundException("UserEmailImage with ID=" + userFormDto.getId() + " not found");
            }

            UserEmailImage userDetailsMailPhoto = optionalUserEmailImage.get();

            // Шаг 5: Обновляем email и image
            userDetailsMailPhoto.setEmail(userFormDto.getEmail());
            if (file.getBytes().length > 0) {
                userDetailsMailPhoto.setUserImage(file.getBytes());
            } else {
                userDetailsMailPhoto.setUserImage(userFormDto.getUserImage());
            }

            // Шаг 6: Сохраняем изменения
            userRepository.save(customUserDetails);
            userEmailRepository.save(userDetailsMailPhoto);

            LOGGER.info("User with ID={} has been updated successfully", userFormDto.getId());
        } catch (Exception e) {
            LOGGER.error("Error updating user: {}", e.getMessage(), e);
            throw e; // Пробрасываем исключения наверх
        }
    }

    /**
     * Проверяет права доступа текущего пользователя.
     *
     * @param customUserDetails Пользователь, которого нужно проверить.
     * @throws AccessDeniedException Если у текущего пользователя нет прав на редактирование.
     */
    private void checkPermission(CustomUserDetails customUserDetails) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No user is logged in");
        }

        String currentUserUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));

        if (!customUserDetails.getUsername().equals(currentUserUsername) && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to edit this user.");
        }
    }

}