package auction.controller;

import auction.exception.ProductNotFoundException;
import auction.model.Product;
import auction.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {
    @Autowired
    public ProductService productService; // for test

    @GetMapping("/products")
    public String getAllProducts(Model model, @RequestParam(required = false) String keyword) {
        List<Product> products = productService.searchProducts(keyword); // Use the service to fetch products
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword); // Add keyword to the model if present
        return "products";
    }

    @GetMapping("/products/new")
    public String addProduct(Model model) {
        Product product = new Product();
        product.setPublished(false);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("userName", authentication.getName()); // Add the username to the model
        }

        model.addAttribute("product", product);
        model.addAttribute("pageTitle", "Create new product");
        return "product_form";
    }

    @PostMapping("/products/save")
    public String saveProduct(Product product, RedirectAttributes redirectAttributes, @RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            product.setUsername(authentication.getName()); // Set the username
        }

        productService.saveProduct(product, file); // Delegate saving logic to the service
        redirectAttributes.addFlashAttribute("message", "The Product has been saved successfully!");
        return "redirect:/products";
    }

    @GetMapping("/products/{id}")
    public String editProduct(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> optionalProduct = productService.getProductById(id);
        if (optionalProduct.isEmpty()) {
            throw new ProductNotFoundException("Product with ID=" + id + " not found");
        }

        Product product = optionalProduct.get();

        if (!productService.hasPermissionToEdit(product)) {
            throw new AccessDeniedException("You do not have permission to edit this product.");
        }

        model.addAttribute("product", product);
        model.addAttribute("pageTitle", "Edit Product (ID: " + id + ")");
        return "product_form";
    }

    @DeleteMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Product> optionalProduct = productService.getProductById(id);
        if (optionalProduct.isEmpty()) {
            throw new ProductNotFoundException("Product with ID=" + id + " not found");
        }

        Product product = optionalProduct.get();

        if (!productService.hasPermissionToEdit(product)) {
            throw new AccessDeniedException("You do not have permission to delete this product.");
        }

        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("message", "The Product with ID=" + id + " has been deleted successfully!");
        return "redirect:/products";
    }

    @GetMapping("/products/{id}/published/{status}")
    public String updateProductPublishedStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean published,
                                               RedirectAttributes redirectAttributes) {
        productService.updatePublishedStatus(id, published); // Delegate status update logic to the service

        String status = published ? "published" : "disabled";
        String message = "The Product id=" + id + " has been " + status;
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/products";
    }

    @DeleteMapping("/deleteImage/{id}") // deleteImage
    public String deleteImage(@PathVariable("id") Integer id) {
        productService.deleteImage(id); // Delegate image deletion logic to the service
        return "redirect:/products/" + id;
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "/access-denied";
    }
}