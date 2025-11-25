package com.smoothOrg.web.controller;

import com.smoothOrg.domain.entity.CartItem;
import com.smoothOrg.domain.entity.User;
import com.smoothOrg.domain.repository.UserRepository;
import com.smoothOrg.services.cart.CartService;
import com.smoothOrg.web.config.PlatformFeesConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/user-cart")
public class UserCartController {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final PlatformFeesConfig platformFeesConfig;

    public UserCartController(CartService cartService, 
                             UserRepository userRepository,
                             PlatformFeesConfig platformFeesConfig) {
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.platformFeesConfig = platformFeesConfig;
    }

    /**
     * Add item to user's cart
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request) {
        Long userId = getCurrentUserId();
        CartItem item = cartService.addToCart(userId, request.productData, request.quantity);
        return ResponseEntity.ok(Map.of("message", "Item added to cart", "cartItem", item));
    }

    /**
     * Get user's cart items
     */
    @GetMapping
    public ResponseEntity<?> getUserCart() {
        Long userId = getCurrentUserId();
        List<CartItem> cartItems = cartService.getUserCart(userId);
        return ResponseEntity.ok(Map.of("cartItems", cartItems));
    }

    /**
     * Update quantity
     */
    @PutMapping("/{cartItemId}/quantity")
    public ResponseEntity<?> updateQuantity(@PathVariable Long cartItemId, 
                                           @RequestBody Map<String, Integer> body) {
        cartService.updateQuantity(cartItemId, body.get("quantity"));
        return ResponseEntity.ok(Map.of("message", "Quantity updated"));
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
    }

    /**
     * Clear entire cart
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        Long userId = getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }

    /**
     * Calculate platform-wise pricing with fees
     */
    @GetMapping("/calculate")
    public ResponseEntity<CartCalculationResponse> calculateCart() {
        Long userId = getCurrentUserId();
        List<CartItem> cartItems = cartService.getUserCart(userId);

        // Convert CartItem entities to product maps for calculation
        List<Map<String, Object>> productMaps = cartItems.stream()
                .map(CartItem::getProductData)
                .collect(Collectors.toList());

        List<Map<String, Object>> calculations = calculatePlatformPricing(productMaps);

        // Sort by total cost
        calculations.sort(Comparator.comparingDouble(c -> (Double) c.get("totalCost")));

        return ResponseEntity.ok(new CartCalculationResponse(calculations));
    }

    // Helper method to calculate platform pricing
    private List<Map<String, Object>> calculatePlatformPricing(List<Map<String, Object>> items) {
        List<Map<String, Object>> calculations = new ArrayList<>();

        // Get all unique platforms from cart items
        Set<String> platforms = items.stream()
                .flatMap(item -> {
                    Object platformsObj = item.get("platforms");
                    if (platformsObj instanceof List) {
                        return ((List<Map<String, Object>>) platformsObj).stream();
                    }
                    return Stream.empty();
                })
                .map(p -> (String) p.get("platform"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (String platform : platforms) {
            PlatformCalculation calc = calculateForPlatform(platform, items);
            calculations.add(calc.toMap());
        }

        return calculations;
    }

    private PlatformCalculation calculateForPlatform(String platform, List<Map<String, Object>> items) {
        PlatformCalculation calc = new PlatformCalculation(platform);
        PlatformFeesConfig.PlatformFee fees = platformFeesConfig.getFeeForPlatform(platform);

        for (Map<String, Object> item : items) {
            Object platformsObj = item.get("platforms");
            if (!(platformsObj instanceof List)) continue;
            
            List<Map<String, Object>> platformsList = (List<Map<String, Object>>) platformsObj;
            
            // Find this platform's pricing
            Optional<Map<String, Object>> platformData = platformsList.stream()
                    .filter(p -> platform.equals(p.get("platform")))
                    .findFirst();

            if (platformData.isPresent()) {
                Map<String, Object> data = platformData.get();
                boolean available = (Boolean) data.getOrDefault("availability", false);
                
                if (available) {
                    double price = ((Number) data.get("selling_price")).doubleValue();
                    calc.addAvailableItem((String) item.get("product_name"), price, data);
                } else {
                    // Find cheapest available alternative
                    Map<String, Object> cheapest = findCheapestAvailable(platformsList);
                    if (cheapest != null) {
                        calc.addFallbackItem(
                                (String) item.get("product_name"),
                                (String) cheapest.get("platform"),
                                ((Number) cheapest.get("selling_price")).doubleValue(),
                                cheapest
                        );
                    } else {
                        calc.addUnavailableItem((String) item.get("product_name"));
                    }
                }
            }
        }

        // Calculate fees
        double deliveryFee = calc.subtotal >= fees.getFreeDeliveryThreshold() ? 0 : fees.getDeliveryFee();
        calc.setFees(deliveryFee, fees.getHandlingCharge(), fees.getPlatformFee());

        return calc;
    }

    private Map<String, Object> findCheapestAvailable(List<Map<String, Object>> platforms) {
        return platforms.stream()
                .filter(p -> (Boolean) p.getOrDefault("availability", false))
                .min(Comparator.comparingDouble(p -> ((Number) p.get("selling_price")).doubleValue()))
                .orElse(null);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    // DTOs
    public record AddToCartRequest(Map<String, Object> productData, Integer quantity) {}
    public record CartCalculationResponse(List<Map<String, Object>> calculations) {}

    static class PlatformCalculation {
        String platform;
        double subtotal = 0;
        double deliveryFee = 0;
        double handlingCharge = 0;
        double platformFee = 0;
        double totalCost = 0;
        int availableItems = 0;
        int unavailableItems = 0;
        List<Map<String, Object>> items = new ArrayList<>();
        boolean isFreeDelivery = false;

        public PlatformCalculation(String platform) {
            this.platform = platform;
        }

        public void addAvailableItem(String name, double price, Map<String, Object> data) {
            subtotal += price;
            availableItems++;
            items.add(Map.of(
                    "name", name,
                    "source", platform,
                    "price", price,
                    "available", true,
                    "data", data
            ));
        }

        public void addFallbackItem(String name, String sourcePlatform, double price, Map<String, Object> data) {
            subtotal += price;
            items.add(Map.of(
                    "name", name,
                    "source", sourcePlatform,
                    "price", price,
                    "available", true,
                    "isFallback", true,
                    "data", data
            ));
        }

        public void addUnavailableItem(String name) {
            unavailableItems++;
            items.add(Map.of(
                    "name", name,
                    "available", false
            ));
        }

        public void setFees(double delivery, double handling, double platform) {
            this.deliveryFee = delivery;
            this.handlingCharge = handling;
            this.platformFee = platform;
            this.isFreeDelivery = delivery == 0;
            this.totalCost = subtotal + delivery + handling + platform;
        }

        public Map<String, Object> toMap() {
            return Map.of(
                    "platform", platform,
                    "subtotal", subtotal,
                    "deliveryFee", deliveryFee,
                    "handlingCharge", handlingCharge,
                    "platformFee", platformFee,
                    "totalCost", totalCost,
                    "availableItems", availableItems,
                    "unavailableItems", unavailableItems,
                    "isFreeDelivery", isFreeDelivery,
                    "items", items
            );
        }
    }
}
