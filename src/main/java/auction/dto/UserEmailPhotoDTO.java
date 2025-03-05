package auction.dto;

import auction.model.Role;
import org.apache.tomcat.util.codec.binary.Base64;

public class UserEmailPhotoDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean enabled;
    private byte[] userImage;

    // Constructors
    public UserEmailPhotoDTO() {
    }

    public UserEmailPhotoDTO(Long id, String username, String email, Role role, boolean enabled, byte[] userImage) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
        this.userImage = userImage;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getUserImage() {
        return userImage;
    }

    public String getImageDataBase64() {
        return Base64.encodeBase64String(this.userImage);
    }

    public void setUserImage(byte[] userImage) {
        this.userImage = userImage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}