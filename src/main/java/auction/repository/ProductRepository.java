package auction.repository;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import auction.model.Product;

import java.util.List;

@Repository
@Transactional
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTitleContainingIgnoreCase(String keyword); // findByTitleContainingIgnoreCase

    @Query(value = "UPDATE products\n" +
            "SET bytes = null\n" +
            "WHERE  id = :id ", nativeQuery = true)
    @Modifying
    public void deleteImage(Integer id);

    @Query("UPDATE Product t SET t.published = :published WHERE t.id = :id")
    @Modifying
    public void updatePublishedStatus(Integer id, boolean published);

}
