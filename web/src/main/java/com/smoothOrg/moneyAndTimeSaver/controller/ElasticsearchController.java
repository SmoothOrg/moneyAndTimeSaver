package com.smoothOrg.moneyAndTimeSaver.controller;

import com.smoothOrg.services.elastic.ElasticsearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

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
}
