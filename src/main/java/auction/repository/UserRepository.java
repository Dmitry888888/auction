package auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import auction.model.CustomUserDetails;

public interface UserRepository extends JpaRepository<CustomUserDetails, Long> {
    CustomUserDetails findByUsername(String username);
    boolean existsByUsername(String username);
}
