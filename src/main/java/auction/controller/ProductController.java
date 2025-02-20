package auction.controller;

import auction.model.Bid;
import auction.model.Product;
import auction.repository.BidRepository;
import auction.repository.ProductRepository;
import auction.repository.UserRepository;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository customUserRepository;
    @Autowired
    private BidRepository bidRepository;

    @GetMapping("/products")
    public String getAll1(Model model, @Param("keyword") String keyword ) {

        try {

            List<Product> products = new ArrayList<Product>();

            if (keyword == null) {
                productRepository.findAll().forEach(products::add);
            } else {
                productRepository.findByTitleContainingIgnoreCase(keyword).forEach(products::add);;
                model.addAttribute("keyword", keyword);
            }

              List<Bid> bids = bidRepository.findAll();
              model.addAttribute("products", products);
              model.addAttribute("bids", bids);
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }

        return "products";
    }


    @GetMapping("/products/new")
    public String addProduct(Model model) {
        Product product = new Product();
        product.setPublished(false);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String userName = authentication.getName(); // Retrieves the username
            Long userId = customUserRepository.findByUsername(userName).getId();
            System.out.println(userName + "   =   " + userId);

            model.addAttribute("userName", userName); // Adds the username to the model
        }
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", "Create new product");

        return "product_form";
    }
    @PostMapping("/products/save")
    public String saveProduct(Product product, RedirectAttributes redirectAttributes, @RequestParam("file") MultipartFile file) {
        try {

              if( file.getBytes().length > 0 ) {
                    file.getBytes();
                    System.out.println(file.getContentType()+file.getSize());
                    product.setBytes(file.getBytes());
              }
              if( product.getCurrentPrice() == null ) {
                    product.setCurrentPrice(product.getStartPrice());
               }

                productRepository.save(product);

                redirectAttributes.addFlashAttribute("message", "The Product has been saved successfully!");
        } catch (Exception e) {
                redirectAttributes.addAttribute("message", e.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/products/{id}")
    public String editProduct(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
              Product product = productRepository.findById(id).get();

              Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
              String currentUserUsername = authentication.getName();

              // Check if the current user is the product owner or has the ADMIN role
              boolean isAdmin = authentication.getAuthorities().stream()
                          .anyMatch(authority -> authority.getAuthority().equals("ADMIN") );
              boolean isOwner = product.getUsername().equals(currentUserUsername);

              if (!isAdmin && !isOwner) {
                  redirectAttributes.addFlashAttribute("message", "You do not have permission to edit this product.");
                  return "redirect:/products";
              }

              model.addAttribute("product", product);
              model.addAttribute("pageTitle", "Edit Product (ID: " + id + ")");

              return "product_form";
        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/products";
          }
    }



    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productRepository.findById(id).get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserUsername = authentication.getName();

            // Check if the current user is the product owner or has the ADMIN role
            boolean isAdmin = authentication.getAuthorities().stream()
                      .anyMatch(authority -> authority.getAuthority().equals("ADMIN") );
            boolean isOwner = product.getUsername().equals(currentUserUsername);

            if (!isAdmin && !isOwner) {
                  redirectAttributes.addFlashAttribute("message", "You do not have permission to DELETE this product.");
                  return "redirect:/products";
            }
            productRepository.deleteById(id);

            redirectAttributes.addFlashAttribute("message", "The Product with id=" + id + " has been deleted successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

      return "redirect:/products";
    }

    @GetMapping("/products/{id}/published/{status}")
    public String updateProductPublishedStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean published,
                                                Model model, RedirectAttributes redirectAttributes) {
        try {
            productRepository.updatePublishedStatus(id, published);

                String status = published ? "published" : "disabled";
                String message = "The Product id=" + id + " has been " + status;

                redirectAttributes.addFlashAttribute("message", message);

        } catch (Exception e) {
                redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/products";
    }
    @GetMapping("/delete/{id}") // deleteImage
    public String deleteImage(@PathVariable("id") Integer id) {
        try {
            productRepository.deleteImage(id);
            System.out.println("done delete");
        } catch (Exception e) {
            System.out.println("done ex");
        }
        return "redirect:/products/{id}";
    }
    @GetMapping("/access-denied")
    public String accessDenied() {
      return "/access-denied";
    }

}
