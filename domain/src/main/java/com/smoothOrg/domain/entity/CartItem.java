package com.smoothOrg.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "product_id")
    private String productId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> productData;

    private Integer quantity = 1;

    @Column(updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    // Constructors
    public CartItem() {}

    public CartItem(User user, String productId, Map<String, Object> productData) {
        this.user = user;
        this.productId = productId;
        this.productData = productData;
        this.quantity = 1;
    }

    public CartItem(User user, Map<String, Object> productData, Integer quantity) {
        this.user = user;
        this.productData = productData;
        this.quantity = quantity;
        if (productData != null) {
            this.productId = (String) productData.get("product_id");
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Map<String, Object> getProductData() {
        return productData;
    }

    public void setProductData(Map<String, Object> productData) {
        this.productData = productData;
        if (productData != null) {
            this.productId = (String) productData.get("product_id");
        }
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}
