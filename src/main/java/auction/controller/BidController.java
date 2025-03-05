package auction.controller;

import auction.model.CustomUserDetails;
import auction.model.Product;
import auction.repository.BidRepository;
import auction.repository.ProductRepository;
import auction.repository.UserRepository;
import auction.service.BidService;
import auction.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;


@Controller
    public class BidController {
        @Autowired
        private ProductRepository productRepository;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        public BidService bidService; // for test public
        @Autowired
        private BidRepository bidRepository;
        @Autowired
        public UserService userService; // for test public
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

            try {
                // Fetch the product by ID
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                // Validate the bid amount
                bidService.validateBid(product, bidAmount);

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
    @DeleteMapping("/cancelBid")
    public String cancelBid(@RequestParam Long productId, RedirectAttributes redirectAttributes) {
        try {
            // Retrieve the current user's username using the UserService
            String currentUserUsername = userService.getCurrentUsername();

            if (currentUserUsername == null) {
                redirectAttributes.addFlashAttribute("message", "You must be logged in to cancel a bid.");
                return "redirect:/products";
            }

            // Delegate the cancellation logic to the BidService
            boolean isCancelled = bidService.cancelBid(productId, currentUserUsername);

            if (isCancelled) {
                redirectAttributes.addFlashAttribute("message", "Your bid has been canceled successfully.");
            } else {
                redirectAttributes.addFlashAttribute("message", "Failed to cancel the bid.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error: " + e.getMessage());
        }
        return "redirect:/products";
    }

}

