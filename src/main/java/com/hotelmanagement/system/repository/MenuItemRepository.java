package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByIsAvailable(boolean isAvailable);

    // Method to find available items by category
    List<MenuItem> findByIsAvailableAndCategoryIgnoreCase(boolean isAvailable, String category);

    /**
     * Updates a menu item using a single, efficient query.
     */
    @Transactional
    @Modifying
    @Query("UPDATE MenuItem m SET " +
            "m.name = :name, " +
            "m.description = :description, " +
            "m.price = :price, " +
            "m.isAvailable = :isAvailable, " +
            "m.imageUrl = :imageUrl, " +
            "m.category = :category " +
            "WHERE m.id = :id")
    void updateMenuItem(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("price") Double price,
            @Param("isAvailable") boolean isAvailable,
            @Param("imageUrl") String imageUrl,
            @Param("category") String category
    );

    List<MenuItem> findByCategoryIgnoreCase(String category);

    @Query("SELECT DISTINCT m.category FROM MenuItem m ORDER BY m.category")
    List<String> findAllDistinctCategories();

    @Query("SELECT m FROM MenuItem m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MenuItem> searchByKeyword(@Param("keyword") String keyword);
}