package com.smoothOrg.moneyAndTimeSaver.controller;

import com.smoothOrg.services.elastic.ElasticsearchService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/elastic")
public class ElasticsearchController {

    private final ElasticsearchService elasticsearchService;

    public ElasticsearchController(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @GetMapping("/indices")
    public List<String> getIndices() throws IOException {
        return elasticsearchService.getAllIndices();
    }

    @GetMapping("/indices/{index}/mapping")
    public String getMapping(@PathVariable String index) throws IOException {
        return elasticsearchService.getMapping(index);
    }

    @GetMapping("/indices/{index}/documents")
    public List<String> getDocuments(@PathVariable String index) throws IOException {
        return elasticsearchService.getAllDocuments(index);
    }

    @GetMapping("/indices/{index}/documents/{id}")
    public String getDocument(@PathVariable String index, @PathVariable String id) throws IOException {
        return elasticsearchService.getDocument(index, id);
    }

    @PostMapping("/indices/{index}/documents/{id}")
    public boolean indexDocument(@PathVariable String index,
                                 @PathVariable String id,
                                 @RequestBody Map<String, Object> document) throws IOException {
        return elasticsearchService.indexDocument(index, id, document);
    }

    @DeleteMapping("/indices/{index}/documents/{id}")
    public boolean deleteDocument(@PathVariable String index, @PathVariable String id) throws IOException {
        return elasticsearchService.deleteDocument(index, id);
    }
}
