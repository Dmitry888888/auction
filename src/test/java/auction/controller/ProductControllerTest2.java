package auction.controller;

import auction.model.Product;
import auction.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProductControllerTest2 {

    private MockMvc mockMvc;

    private ProductService productService;

    private ProductController productController;

    @BeforeEach
    public void setUp() {
        // Initialize mocks
        productService = Mockito.mock(ProductService.class);

        // Create the controller instance with mocked dependencies
        productController = new ProductController();
        productController.productService = productService;

        // Set up MockMvc with the controller
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    public void testGetAllProductsNoKeyword() throws Exception {
        // Arrange
        List<Product> mockProducts = Arrays.asList(
                new Product(),
                new Product()
        );

        when(productService.searchProducts(null)).thenReturn(mockProducts); // Mock the service method

        // Act & Assert
        mockMvc.perform(get("/products")) // Simulate a GET request to /products
                .andExpect(status().isOk()) // Expect HTTP status 200 (OK)
                .andExpect(view().name("products")) // Expect the "products" view name
                .andExpect(model().attribute("products", mockProducts)) // Verify the "products" attribute in the model
                .andExpect(model().attribute("keyword", null)); // Verify the "keyword" attribute is null
    }

    @Test
    public void testGetAllProductsWithKeyword() throws Exception {
        // Arrange
        String keyword = "test";
        List<Product> mockProducts = Arrays.asList(
                new Product()
        );

        when(productService.searchProducts(keyword)).thenReturn(mockProducts); // Mock the service method

        // Act & Assert
        mockMvc.perform(get("/products").param("keyword", keyword)) // Simulate a GET request with a keyword
                .andExpect(status().isOk()) // Expect HTTP status 200 (OK)
                .andExpect(view().name("products")) // Expect the "products" view name
                .andExpect(model().attribute("products", mockProducts)) // Verify the "products" attribute in the model
                .andExpect(model().attribute("keyword", keyword)); // Verify the "keyword" attribute matches the provided keyword
    }

    @Test
    public void testGetAllProductsExceptionThrown() throws Exception {
        // Arrange
        String errorMessage = "Database error";
        when(productService.searchProducts(null)).thenThrow(new RuntimeException(errorMessage)); // Simulate an exception

        // Act & Assert
        mockMvc.perform(get("/products")) // Simulate a GET request to /products
                .andExpect(status().isOk()) // Expect HTTP status 200 (OK)
                .andExpect(view().name("products")) // Expect the "products" view name
                .andExpect(model().attribute("message", errorMessage)); // Verify the "message" attribute contains the error message
    }
}