package auction.service;

import auction.model.Bid;
import auction.model.Product;
import auction.repository.BidRepository;
import auction.repository.ProductRepository;
import auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BidService {

    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductRepository productRepository;

    /**
     * Cancels the highest active bid for a product made by the specified user.
     *
     * @param productId The ID of the product.
     * @param username  The username of the current user.
     * @return True if the bid was successfully canceled, false otherwise.
     */
    @Transactional
    public boolean cancelBid(Long productId, String username) {
        Long userId = userService.getCurrentUserId();
        try {
            // Step 1: Find all bids for the product made by the user
            List<Bid> bids = bidRepository.findByUserIdAndProductId(userId, productId);

            // Step 2: Find the highest active bid
            Bid maxBid = null;
            BigDecimal maxAmount = BigDecimal.ZERO;

            for (Bid bid : bids) {
                if (!bid.isCancelled()) {
                    if (maxBid == null || bid.getAmount().compareTo(maxAmount) > 0) {
                        maxBid = bid;
                        maxAmount = bid.getAmount();
                    }
                }
            }

            if (maxBid == null) {
                return false; // User hasn't placed any active bids
            }

            // Step 3: Fetch the associated product
            Optional<Product> optionalProduct = productRepository.findById(productId);
            if (optionalProduct.isEmpty() || optionalProduct.get().getCurrentPrice() == null) {
                throw new RuntimeException("Product details are incomplete.");
            }
            Product product = optionalProduct.get();

            // Step 4: Validate that the bid can be canceled
            if (maxBid.getAmount().compareTo(product.getCurrentPrice()) < 0) {
                return false; // Bid is not the highest
            }

            if (!maxBid.getUser().getUsername().equals(username)) {
                return false; // Bid does not belong to the current user
            }

            // Step 5: Cancel the bid and update the product's current price
            maxBid.setCancelled(true);
            bidRepository.save(maxBid); // Save the canceled bid

            // Step 6: Find the next highest bid
            BigDecimal newCurrentPrice = findPreviousHighestBid(productId, maxBid.getAmount());

            // Step 7: Update the product's current price
            product.setCurrentPrice(newCurrentPrice);
            productRepository.save(product);

            return true; // Bid successfully canceled
        } catch (Exception e) {
            System.out.println("Error canceling bid: " + e.getMessage());
            return false;
        }
    }

    /**
     * Finds the previous highest bid for a product, excluding the specified amount.
     *
     * @param productId         The ID of the product.
     * @param excludedBidAmount The amount of the bid being canceled.
     * @return The new current price for the product.
     */
    public BigDecimal findPreviousHighestBid(Long productId, BigDecimal excludedBidAmount) {
        // Fetch all active bids for the product, excluding the one being canceled
        List<Bid> remainingBids = bidRepository.findAllByProductIdAndCancelledFalseAndAmountLessThan(productId, excludedBidAmount);

        // Determine the highest remaining bid
        return remainingBids.stream()
                .map(Bid::getAmount)
                .max(BigDecimal::compareTo)
                .orElseGet(() -> productRepository.findById(productId).map(Product::getStartPrice)
                        .orElseThrow(() -> new RuntimeException("No fallback price available"))); // Fallback to start price if no bids remain
    }

    /**
     * Validates a bid based on product rules.
     *
     * @param product   The product being bid on.
     * @param bidAmount The bid amount.
     */
    public void validateBid(Product product, BigDecimal bidAmount) {
        // Rule 1: Bid must be greater than or equal to the current price + bid step
        BigDecimal minAllowedBid = product.getCurrentPrice().add(BigDecimal.valueOf(5));
        if (bidAmount.compareTo(minAllowedBid) < 0) {
            throw new IllegalArgumentException("Your bid must be at least " + minAllowedBid + ".");
        }

        if (bidAmount.compareTo(product.getStartPrice()) < 0) {
            throw new IllegalArgumentException("Your bid must be at least (start price) " + product.getStartPrice() + ".");
        }

        // Rule 2: Bid must follow the step increment (e.g., increments of 5)
        BigDecimal remainder = bidAmount.subtract(product.getCurrentPrice()).remainder(BigDecimal.valueOf(5));
        if (remainder.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Your bid must be in increments of 5.");
        }
    }

    /**
     * Places a new bid for a product.
     *
     * @param userId The ID of the user placing the bid.
     * @param lotId  The ID of the product (lot).
     * @param amount The bid amount.
     */
    @Transactional
    public void placeBid(Long userId, Long lotId, BigDecimal amount) {
        try {
            Product product = productRepository.findById(lotId).orElseThrow(() -> new RuntimeException("Product not found"));
            validateBid(product, amount);

            Bid bid = new Bid();
            bid.setAmount(amount);
            bid.setUser(userRepository.getReferenceById(userId));
            bid.setProduct(product);

            bidRepository.save(bid); // Save the new bid
            product.setCurrentPrice(amount); // Update the product's current price
            productRepository.save(product);
        } catch (Exception e) {
            System.out.println("Error placing bid: " + e.getMessage());
        }
    }
}