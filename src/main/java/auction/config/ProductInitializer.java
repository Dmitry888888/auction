package auction.config;

import auction.model.Product;
import auction.repository.ProductRepository;
import auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;

@Component
public class ProductInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if products already exist to avoid duplicates
        if (productRepository.count() == 0) {
            createProductForUser("adminTest", "static/images/product1.png", "Product 1", "This is the first product.", new BigDecimal("100.00"), new BigDecimal("150.00"));
            createProductForUser("userTest", "static/images/product2.png", "Product 2", "This is the second product.", new BigDecimal("200.00"), new BigDecimal("250.00"));
        }
    }

    private void createProductForUser(String username, String imagePath, String title, String description, BigDecimal startPrice, BigDecimal currentPrice) throws IOException {
        // Load the image file from the classpath
        byte[] imageBytes = Files.readAllBytes(new ClassPathResource(imagePath).getFile().toPath());

        // Create a new product
        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPublished(true); // Set to true if the product should be published immediately
        product.setBytes(imageBytes); // Store the image bytes
        product.setUsername(username); // Assign the owner of the product
        product.setStartPrice(startPrice); // Set the start price
        product.setCurrentPrice(currentPrice); // Set the current price

        // Save the product to the database
        productRepository.save(product);

        System.out.println("Created product: " + title + " for user: " + username);
    }
}
