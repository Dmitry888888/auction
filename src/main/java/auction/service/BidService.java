package auction.service;


import auction.model.Bid;
import auction.model.CustomUserDetails;
import auction.model.Product;
import auction.repository.BidRepository;
import auction.repository.ProductRepository;
import auction.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BidService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BidRepository bidRepository;
    private static final Logger LOGGER = LogManager.getLogger(BidService.class);
    @Transactional
    public void placeBid(Long userId, Long lotId, BigDecimal amount) {
        try {
            CustomUserDetails user = userRepository.getReferenceById(userId);
            Product product = productRepository.getReferenceById(lotId);

            Bid bid = new Bid();
            bid.setAmount(amount);
            bid.setUser(user);
            bid.setProduct(product);

            bidRepository.save(bid);
            product.setCurrentPrice(amount);
            productRepository.save(product);
        } catch (Exception e) {
            LOGGER.warn("placeBid ", e.getMessage());
        }
    }
}