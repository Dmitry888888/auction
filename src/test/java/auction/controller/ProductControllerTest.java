package auction.controller;

import auction.model.Product;
import auction.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDeleteProductSuccessfulDeletion() {
        // Arrange
        Long productId = 1L;
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        when(productService.getProductById(productId)).thenReturn(Optional.of(new Product()));
        when(productService.hasPermissionToEdit(any())).thenReturn(true);

        // Act
        String result = productController.deleteProduct(productId, redirectAttributes);

        // Assert
        assertEquals("redirect:/products", result);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("message"), eq("The Product with id=1 has been deleted successfully!"));
        verify(productService, times(1)).deleteProduct(productId);
    }

    @Test
    public void testDeleteProductProductNotFound() {
        // Arrange
        Long productId = 1L;
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        when(productService.getProductById(productId)).thenReturn(Optional.empty());

        // Act
        String result = productController.deleteProduct(productId, redirectAttributes);

        // Assert
        assertEquals("redirect:/products", result);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("message"), eq("Product not found"));
        verify(productService, never()).deleteProduct(productId);
    }

    @Test
    public void testDeleteProductNoPermission() {
        // Arrange
        Long productId = 1L;
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        Product product = new Product();
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        when(productService.hasPermissionToEdit(product)).thenReturn(false);

        // Act
        String result = productController.deleteProduct(productId, redirectAttributes);

        // Assert
        assertEquals("redirect:/products", result);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("message"), eq("You do not have permission to delete this product."));
        verify(productService, never()).deleteProduct(productId);
    }
}
