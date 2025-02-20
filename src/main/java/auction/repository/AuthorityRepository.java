package auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import auction.model.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

}
