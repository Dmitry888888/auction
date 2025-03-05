package auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import auction.model.UserEmailImage;

public interface UserEmailRepository extends JpaRepository<UserEmailImage, Long> {
    UserEmailImage findByEmail(String email);

    UserEmailImage findByUserId(Long userId);

}