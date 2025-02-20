package auction.controller;

import auction.model.Bid;
import auction.model.CustomUserDetails;
import auction.model.Product;
import auction.repository.BidRepository;
import auction.repository.ProductRepository;
import auction.repository.UserRepository;
import auction.service.BidService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Controller
    public class BidController {
        @Autowired
        private ProductRepository productRepository;
        @Autowired
        private UserRepository userRepository;

        @Autowired
        private BidService bidService;
        @Autowired
        private BidRepository bidRepository;
        @Transactional
        @PostMapping("/bids/{productId}")
        public String placeBid(@PathVariable Long productId,
                               @RequestParam BigDecimal bidAmount,
                               @Param("keyword") String keyword,
                               Model model) {


            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails currentUser = null;
            if (authentication != null && authentication.isAuthenticated()) {
                String userName = authentication.getName(); // Retrieves the username
                currentUser = userRepository.findByUsername(userName);

            }
            System.out.println(currentUser.getUsername());

            try {
                // Fetch the product by ID
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                // Validate the bid amount
                validateBid(product, bidAmount);

                // Place the bid (business logic)
                bidService.placeBid(currentUser.getId(), productId, bidAmount);

                // Redirect to success page or back to the product list
                return "redirect:/products";

            } catch (IllegalArgumentException e) {
                // Handle validation errors
                model.addAttribute("errorMessage", e.getMessage());
                model.addAttribute("product", productRepository.findById(productId).orElse(null));
                model.addAttribute("productId", productId);
                // Reload the product
                return "bids"; // Return to the same bid form with the error message
            }
        }
    private void validateBid(Product product, BigDecimal bidAmount) {
        // Rule 1: Bid must be greater than or equal to the current price + bid step
        BigDecimal minAllowedBid = product.getCurrentPrice().add(BigDecimal.valueOf(5));
        if (bidAmount.compareTo(minAllowedBid) < 0 ) {
            throw new IllegalArgumentException("Your bid must be at least " + minAllowedBid + ".");
        }
        if (bidAmount.compareTo(product.getStartPrice()) < 0 ) {
            throw new IllegalArgumentException("Your bid must be at least (start price) " + product.getStartPrice() + ".");
        }

        // Rule 2: Bid must follow the step increment (e.g., increments of 5)
        BigDecimal remainder = bidAmount.subtract(product.getCurrentPrice()).remainder(BigDecimal.valueOf(5));
        if (remainder.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Your bid must be in increments of 5.");
        }
    }
        @Transactional
        @GetMapping("/bids")
        public String placeBid(@RequestParam Long productId, Model model) {

            Product product = productRepository.getReferenceById(productId);
            BigDecimal bidAmount = product.getCurrentPrice();

            model.addAttribute("product", product);
            model.addAttribute("bidAmount", bidAmount);
            model.addAttribute("productId", productId);
            return "bids";
        }

    @Transactional
    @GetMapping("/cancelBid") // /{bidId}
        public String cancelBid(@RequestParam Long productId, RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String userName = authentication.getName(); // Retrieves the username
            currentUser = userRepository.findByUsername(userName);
        }
//        String currentUserUsername = currentUser.getUsername(); // Retrieve username of currently logged-in user alternative
        String currentUserUsername = getCurrentUserUsername(); // Retrieve username of currently logged-in user

        List<Bid> bids = new ArrayList<>();
        bids = bidRepository.findByUserIdAndProductId(currentUser.getId(),productId);

        Bid maxBid = null; // Variable to store the bid with the maximum amount
        BigDecimal maxAmount = BigDecimal.ZERO; // Initialize max amount to zero

        for (Bid bid:bids) {
            if (!bid.isCancelled()) { // Check if the bid is not cancelled
                if (maxBid == null || bid.getAmount().compareTo(maxAmount) > 0) {
                    maxBid = bid; // Update maxBid if current bid has a higher amount
                    maxAmount = bid.getAmount(); // Update maxAmount accordingly
                }
            }
        }

        Product product = productRepository.getReferenceById(productId);

        if (maxBid == null ) { // пользователь не сделал ставку на товар
            redirectAttributes.addFlashAttribute("message", "You haven't bid this Item.");
            return "redirect:/products";
        }

        if (maxBid.getAmount().compareTo(product.getCurrentPrice()) < 0){ // попытка отменить ставку, которая уже не является максимальной
            redirectAttributes.addFlashAttribute("message", "Your last bid wasn't biggest");
            return "redirect:/products";
        }

        Bid bid = maxBid;


//        Bid bid = optionalBid.get();
        // Step 2: Fetch the associated product

        if (product == null || product.getCurrentPrice() == null) { // на всякий случай если продуктов нет
            throw new RuntimeException("Product details are incomplete");
        }

        // Step 3: Check if the bid belongs to the current user

        if (!bid.getUser().getUsername().equals(currentUserUsername)) {
            redirectAttributes.addFlashAttribute("message", "Your bid wasn't last");
            return "redirect:/products";
        }

        // Step 4: Check if the bid amount equals the product's current price
        if (bid.getAmount().compareTo(product.getCurrentPrice()) == 0 && !bid.isCancelled()) {
            System.out.println("Canceling bid because its amount matches the product's current price and is owned by the current user.");

            // Step 5: Mark the bid as canceled
            bid.setCancelled(true);
            bidRepository.save(bid);

            // Step 6: Find the next highest bid for the same product
            BigDecimal newCurrentPrice = findPreviousHighestBid(product.getId(), bid.getAmount());

            // Step 7: Update the product's current price
            product.setCurrentPrice(newCurrentPrice);
            productRepository.save(product);
            redirectAttributes.addFlashAttribute("message", " Bid was cancelled: Updated product's current price to: " + newCurrentPrice);
            System.out.println("Updated product's current price to: " + newCurrentPrice);
        } else {
            System.out.println("Bid cannot be canceled or does not match the product's current price.");
        }

        return "redirect:/products";
        }
    private String getCurrentUserUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName(); // Gets the name of the currently authenticated user <button class="citation-flag" data-index="6">
    }
    private BigDecimal findPreviousHighestBid(Long productId, BigDecimal excludedBidAmount) {
        // Fetch all active bids for the product, excluding the one being canceled
        List<Bid> remainingBids = bidRepository.findAllByProductIdAndCancelledFalseAndAmountLessThan(productId, excludedBidAmount);

        // Determine the highest remaining bid // map как работает, не до конца понял
        return remainingBids.stream()
                .map(Bid::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(productRepository.findById(productId).map(Product::getStartPrice)
                        .orElseThrow(() -> new RuntimeException("No fallback price available"))); // Fallback to start price if no bids remain
    }
}

