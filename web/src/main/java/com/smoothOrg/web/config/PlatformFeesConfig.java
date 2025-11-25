package com.smoothOrg.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:platform-fees.properties")
@ConfigurationProperties
public class PlatformFeesConfig {

    private Map<String, PlatformFee> platforms = new HashMap<>();
    private double gstPercent;
    private double packagingCharge;
    
    // External API configuration (for future use)
    private boolean feesApiEnabled = false;
    private String feesApiUrl;

    public static class PlatformFee {
        private double deliveryFee;
        private double freeDeliveryThreshold;
        private double handlingCharge;
        private double platformFee;
        private double surgeMultiplier = 1.0;

        // Getters and Setters
        public double getDeliveryFee() { return deliveryFee; }
        public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

        public double getFreeDeliveryThreshold() { return freeDeliveryThreshold; }
        public void setFreeDeliveryThreshold(double freeDeliveryThreshold) { 
            this.freeDeliveryThreshold = freeDeliveryThreshold; 
        }

        public double getHandlingCharge() { return handlingCharge; }
        public void setHandlingCharge(double handlingCharge) { this.handlingCharge = handlingCharge; }

        public double getPlatformFee() { return platformFee; }
        public void setPlatformFee(double platformFee) { this.platformFee = platformFee; }

        public double getSurgeMultiplier() { return surgeMultiplier; }
        public void setSurgeMultiplier(double surgeMultiplier) { this.surgeMultiplier = surgeMultiplier; }

        /**
         * Calculate total fees for a given cart subtotal
         */
        public double calculateTotalFees(double subtotal) {
            double delivery = subtotal >= freeDeliveryThreshold ? 0 : deliveryFee * surgeMultiplier;
            return delivery + handlingCharge + platformFee;
        }
    }

    // Getters and Setters
    public Map<String, PlatformFee> getPlatforms() { return platforms; }
    public void setPlatforms(Map<String, PlatformFee> platforms) { this.platforms = platforms; }

    public double getGstPercent() { return gstPercent; }
    public void setGstPercent(double gstPercent) { this.gstPercent = gstPercent; }

    public double getPackagingCharge() { return packagingCharge; }
    public void setPackagingCharge(double packagingCharge) { this.packagingCharge = packagingCharge; }

    public PlatformFee getFeeForPlatform(String platform) {
        return platforms.getOrDefault(platform.toLowerCase(), new PlatformFee());
    }

    /**
     * Update platform fees from external API
     * Call this method when external API provides updated fee data
     */
    public void updatePlatformFees(String platform, PlatformFee newFees) {
        platforms.put(platform.toLowerCase(), newFees);
    }

    /**
     * Bulk update all platform fees from external API response
     */
    public void bulkUpdateFees(Map<String, PlatformFee> updatedFees) {
        updatedFees.forEach((platform, fees) -> 
            platforms.put(platform.toLowerCase(), fees)
        );
    }
    
    // API Configuration getters/setters
    public boolean isFeesApiEnabled() { return feesApiEnabled; }
    public void setFeesApiEnabled(boolean feesApiEnabled) { this.feesApiEnabled = feesApiEnabled; }
    
    public String getFeesApiUrl() { return feesApiUrl; }
    public void setFeesApiUrl(String feesApiUrl) { this.feesApiUrl = feesApiUrl; }
}
