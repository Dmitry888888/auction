package auction.repository;

import auction.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByUserIdAndProductId(Long userId, Long productId);
    List<Bid> findAllByProductIdAndCancelledFalseAndAmountLessThan(Long productId, BigDecimal amount);

}
