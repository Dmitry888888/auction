package auction.service;

import auction.model.Product;
import auction.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Retrieve all products.
     *
     * @return A list of all products.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Search products by title containing the keyword (case-insensitive).
     *
     * @param keyword The search keyword.
     * @return A list of matching products.
     */
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.findByTitleContainingIgnoreCase(keyword);
    }

    /**
     * Save or update a product.
     *
     * @param product The product to save.
     * @param file    The uploaded image file.
     * @return The saved product.
     */
    public Product saveProduct(Product product, MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                product.setBytes(file.getBytes()); // Set the image bytes if a file is provided
            }
            if (product.getCurrentPrice() == null) {
                product.setCurrentPrice(product.getStartPrice()); // Set current price if not provided
            }
            return productRepository.save(product); // Save the product
        } catch (Exception e) {
            throw new RuntimeException("Error saving product: " + e.getMessage(), e);
        }
    }

    /**
     * Get a product by ID.
     *
     * @param id The product ID.
     * @return The product with the given ID.
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Delete a product by ID.
     *
     * @param id The product ID.
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * Update the published status of a product.
     *
     * @param id        The product ID.
     * @param published The new published status.
     */
    public void updatePublishedStatus(Integer id, boolean published) {
        productRepository.updatePublishedStatus(id, published);
    }

    /**
     * Delete the image of a product by ID.
     *
     * @param id The product ID.
     */
    public void deleteImage(Integer id) {
        productRepository.deleteImage(id);
    }

    /**
     * Check if the current user has permission to edit or delete a product.
     *
     * @param product The product to check.
     * @return True if the user has permission, false otherwise.
     */
    public boolean hasPermissionToEdit(Product product) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false; // No user is logged in
        }

        String currentUserUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
        boolean isOwner = product.getUsername().equals(currentUserUsername);

        return isAdmin || isOwner; // Return true if admin or owner
    }
}