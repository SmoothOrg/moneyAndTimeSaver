package com.smoothOrg.web.controller;

import com.smoothOrg.services.elastic.ElasticsearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private String resolveIndex(String requestedIndex) {
        return StringUtils.hasText(requestedIndex) ? requestedIndex : defaultIndex;
    }

    public record ProductSearchResponse(String index,
                                        String query,
                                        String geohash,
                                        List<Map<String, Object>> results) {
    }
}
