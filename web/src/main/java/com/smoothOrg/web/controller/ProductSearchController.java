package com.smoothOrg.web.controller;

import com.smoothOrg.services.elastic.ElasticsearchService;
import com.smoothOrg.services.util.GeohashUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductSearchController {

    private final ElasticsearchService elasticsearchService;
    private final String defaultIndex;

    public ProductSearchController(ElasticsearchService elasticsearchService,
                                   @Value("${app.elasticsearch.products-index:grocery_products_v1}") String defaultIndex) {
        this.elasticsearchService = elasticsearchService;
        this.defaultIndex = defaultIndex;
    }

    @GetMapping("/search")
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestParam("query") String query,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "index", required = false) String index) throws IOException {
        String targetIndex = resolveIndex(index);
        List<Map<String, Object>> results = elasticsearchService.searchProducts(targetIndex, query, size);
        return ResponseEntity.ok(new ProductSearchResponse(targetIndex, query, null, results));
    }

    @GetMapping("/search/by-geohash")
    public ResponseEntity<ProductSearchResponse> searchProductsByGeohash(
            @RequestParam("query") String query,
            @RequestParam("geohash") String geohash,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "index", required = false) String index) throws IOException {
        String targetIndex = resolveIndex(index);
        List<Map<String, Object>> results = elasticsearchService.searchProductsByGeohash(targetIndex, query, geohash, size);
        return ResponseEntity.ok(new ProductSearchResponse(targetIndex, query, geohash, results));
    }

    /**
     * Search products by location (latitude/longitude).
     * Automatically converts lat/long to geohash.
     */
    @GetMapping("/search/by-location")
    public ResponseEntity<ProductSearchResponse> searchProductsByLocation(
            @RequestParam("query") String query,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "precision", defaultValue = "7") int precision,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "index", required = false) String index) throws IOException {
        
        // Convert lat/long to geohash with 7-char precision (~150m area)
        String geohash = GeohashUtils.encode(latitude, longitude, precision);
        
        String targetIndex = resolveIndex(index);
        List<Map<String, Object>> results = elasticsearchService.searchProductsByGeohash(targetIndex, query, geohash, size);
        return ResponseEntity.ok(new ProductSearchResponse(targetIndex, query, geohash, results));
    }

    private String resolveIndex(String requestedIndex) {
        return StringUtils.hasText(requestedIndex) ? requestedIndex : defaultIndex;
    }

    public record ProductSearchResponse(String index,
                                        String query,
                                        String geohash,
                                        List<Map<String, Object>> results) {
    }
}
