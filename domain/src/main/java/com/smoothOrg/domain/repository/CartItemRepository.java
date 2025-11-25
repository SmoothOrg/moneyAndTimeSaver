package com.smoothOrg.domain.repository;

import com.smoothOrg.domain.entity.CartItem;
import com.smoothOrg.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    List<CartItem> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    
    // Now we can use the productId field directly instead of JSON query
    Optional<CartItem> findByUserIdAndProductId(Long userId, String productId);
}
