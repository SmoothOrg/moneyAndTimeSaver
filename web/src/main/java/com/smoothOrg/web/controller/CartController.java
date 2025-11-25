package com.smoothOrg.web.controller;

import com.smoothOrg.web.config.PlatformFeesConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final PlatformFeesConfig platformFeesConfig;

    public CartController(PlatformFeesConfig platformFeesConfig) {
        this.platformFeesConfig = platformFeesConfig;
    }

    @PostMapping("/calculate")
    public ResponseEntity<CartCalculationResponse> calculateCart(@RequestBody CartCalculationRequest request) {
        List<Map<String, Object>> calculations = new ArrayList<>();

        Set<String> allPlatforms = request.items.stream()
                .flatMap(item -> ((List<Map<String, Object>>) item.get("platforms")).stream())
                .map(p -> (String) p.get("platform"))
                .collect(Collectors.toSet());

        // Single platform combos
        for (String platform : allPlatforms) {
            PlatformCalculation calc = calculateSinglePlatformCombo(platform, request.items);
            calculations.add(calc.toMap());
        }

        // Optimal combo
        PlatformCalculation optimalCombo = calculateOptimalCombo(request.items);
        calculations.add(optimalCombo.toMap());

        calculations.sort(Comparator.comparingDouble(c -> (Double) c.get("totalCost")));
        return ResponseEntity.ok(new CartCalculationResponse(calculations));
    }

    private PlatformCalculation calculateSinglePlatformCombo(String platform, List<Map<String, Object>> items) {
        PlatformCalculation calc = new PlatformCalculation(platform);

        for (Map<String, Object> item : items) {
            List<Map<String, Object>> platformsList = (List<Map<String, Object>>) item.get("platforms");
            Optional<Map<String, Object>> platformData = platformsList.stream()
                    .filter(p -> platform.equals(p.get("platform")))
                    .findFirst();

            if (platformData.isPresent() && (Boolean) platformData.get().getOrDefault("availability", false)) {
                double price = ((Number) platformData.get().get("selling_price")).doubleValue();
                calc.addItem((String) item.get("product_name"), platform, price, platformData.get(), false);
            } else {
                Map<String, Object> cheapest = findCheapestAvailable(item);
                if (cheapest != null) {
                    String fallbackPlatform = (String) cheapest.get("platform");
                    double price = ((Number) cheapest.get("selling_price")).doubleValue();
                    calc.addItem((String) item.get("product_name"), fallbackPlatform, price, cheapest, true);
                } else {
                    calc.addUnavailableItem((String) item.get("product_name"));
                }
            }
        }

        calculateAllFees(calc);
        return calc;
    }

    private PlatformCalculation calculateOptimalCombo(List<Map<String, Object>> items) {
        PlatformCalculation calc = new PlatformCalculation("🎯 Best Combo");

        for (Map<String, Object> item : items) {
            Map<String, Object> cheapest = findCheapestAvailable(item);
            if (cheapest != null) {
                String platform = (String) cheapest.get("platform");
                double price = ((Number) cheapest.get("selling_price")).doubleValue();
                calc.addItem((String) item.get("product_name"), platform, price, cheapest, false);
            } else {
                calc.addUnavailableItem((String) item.get("product_name"));
            }
        }

        calculateAllFees(calc);
        return calc;
    }

    private void calculateAllFees(PlatformCalculation calc) {
        Map<String, Double> deliveryFees = new HashMap<>();
        Map<String, Double> handlingFees = new HashMap<>();
        Map<String, Double> platformFees = new HashMap<>();

        for (String usedPlatform : calc.getUsedPlatforms()) {
            PlatformFeesConfig.PlatformFee fee = platformFeesConfig.getFeeForPlatform(usedPlatform);
            double platformSubtotal = calc.getSubtotalForPlatform(usedPlatform);

            double deliveryFee = platformSubtotal >= fee.getFreeDeliveryThreshold() ? 0 : fee.getDeliveryFee();
            deliveryFees.put(usedPlatform, deliveryFee);
            handlingFees.put(usedPlatform, fee.getHandlingCharge());
            platformFees.put(usedPlatform, fee.getPlatformFee());
        }

        calc.setFees(deliveryFees, handlingFees, platformFees);
    }

    private Map<String, Object> findCheapestAvailable(Map<String, Object> item) {
        List<Map<String, Object>> platforms = (List<Map<String, Object>>) item.get("platforms");
        return platforms.stream()
                .filter(p -> (Boolean) p.getOrDefault("availability", false))
                .min(Comparator.comparingDouble(p -> ((Number) p.get("selling_price")).doubleValue()))
                .orElse(null);
    }

    public record CartCalculationRequest(List<Map<String, Object>> items) {}
    public record CartCalculationResponse(List<Map<String, Object>> calculations) {}

    static class PlatformCalculation {
        String platform;
        double subtotal = 0;
        Map<String, Double> deliveryFees = new HashMap<>();
        Map<String, Double> handlingFees = new HashMap<>();
        Map<String, Double> platformFees = new HashMap<>();
        double totalCost = 0;
        int availableItems = 0;
        int unavailableItems = 0;
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Double> platformSubtotals = new HashMap<>();

        public PlatformCalculation(String platform) {
            this.platform = platform;
        }

        public void addItem(String name, String sourcePlatform, double price, Map<String, Object> data, boolean isFallback) {
            subtotal += price;
            availableItems++;
            platformSubtotals.merge(sourcePlatform, price, Double::sum);
            
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", name);
            itemMap.put("source", sourcePlatform);
            itemMap.put("price", price);
            itemMap.put("available", true);
            itemMap.put("isFallback", isFallback);
            itemMap.put("data", data);
            items.add(itemMap);
        }

        public void addUnavailableItem(String name) {
            unavailableItems++;
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", name);
            itemMap.put("available", false);
            items.add(itemMap);
        }

        public Set<String> getUsedPlatforms() {
            return platformSubtotals.keySet();
        }

        public double getSubtotalForPlatform(String platformName) {
            return platformSubtotals.getOrDefault(platformName, 0.0);
        }

        public void setFees(Map<String, Double> deliveryFeesMap, Map<String, Double> handlingFeesMap, Map<String, Double> platformFeesMap) {
            this.deliveryFees = deliveryFeesMap;
            this.handlingFees = handlingFeesMap;
            this.platformFees = platformFeesMap;
            
            double totalDelivery = deliveryFeesMap.values().stream().mapToDouble(Double::doubleValue).sum();
            double totalHandling = handlingFeesMap.values().stream().mapToDouble(Double::doubleValue).sum();
            double totalPlatformFee = platformFeesMap.values().stream().mapToDouble(Double::doubleValue).sum();
            
            this.totalCost = subtotal + totalDelivery + totalHandling + totalPlatformFee;
        }

        public Map<String, Object> toMap() {
            double totalDeliveryFee = deliveryFees.values().stream().mapToDouble(Double::doubleValue).sum();
            double totalHandlingFee = handlingFees.values().stream().mapToDouble(Double::doubleValue).sum();
            double totalPlatformFee = platformFees.values().stream().mapToDouble(Double::doubleValue).sum();
            
            Map<String, Object> result = new HashMap<>();
            result.put("platform", platform);
            result.put("subtotal", subtotal);
            result.put("deliveryFee", totalDeliveryFee);
            result.put("deliveryFeeBreakdown", deliveryFees);
            result.put("handlingCharge", totalHandlingFee);
            result.put("handlingFeeBreakdown", handlingFees);
            result.put("platformFee", totalPlatformFee);
            result.put("platformFeeBreakdown", platformFees);
            result.put("platformSubtotals", platformSubtotals);
            result.put("totalCost", totalCost);
            result.put("availableItems", availableItems);
            result.put("unavailableItems", unavailableItems);
            result.put("isFreeDelivery", totalDeliveryFee == 0);
            result.put("items", items);
            
            return result;
        }
    }
}
