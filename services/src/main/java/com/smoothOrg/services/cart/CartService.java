package com.smoothOrg.services.cart;

import com.smoothOrg.domain.entity.CartItem;
import com.smoothOrg.domain.entity.User;
import com.smoothOrg.domain.repository.CartItemRepository;
import com.smoothOrg.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartItem addToCart(Long userId, Map<String, Object> productData, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String productId = (String) productData.get("product_id");
        
        // Check if product already in cart
        return cartItemRepository.findByUserIdAndProductId(userId, productId)
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity() + quantity);
                    return cartItemRepository.save(existingItem);
                })
                .orElseGet(() -> {
                    CartItem newItem = new CartItem(user, productData, quantity);
                    return cartItemRepository.save(newItem);
                });
    }

    public List<CartItem> getUserCart(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
