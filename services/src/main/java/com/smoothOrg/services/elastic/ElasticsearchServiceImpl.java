package com.smoothOrg.services.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.GetMappingRequest;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.get_mapping.IndexMappingRecord;
import co.elastic.clients.elasticsearch.cat.IndicesRequest;
import co.elastic.clients.elasticsearch.cat.IndicesResponse;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    private final ElasticsearchClient client;

    @Autowired
    public ElasticsearchServiceImpl(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public boolean createIndex(String index) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest.Builder()
                .index(index)
                .build();
        CreateIndexResponse response = client.indices().create(request);
        return response.acknowledged();
    }

    @Override
    public boolean updateMapping(String index, String mappingJson) throws IOException {
        PutMappingRequest request = new PutMappingRequest.Builder()
                .index(index)
                .withJson(new StringReader(mappingJson))
                .build();
        PutMappingResponse response = client.indices().putMapping(request);
        return response.acknowledged();
    }

    @Override
    public String getDocument(String index, String id) throws IOException {
        GetRequest request = new GetRequest.Builder()
                .index(index)
                .id(id)
                .build();
        GetResponse<JsonData> response = client.get(request, JsonData.class);
        if (response.found()) {
            // Convert the JsonData payload to its JSON representation.
            return response.source().toString();
        }
        return null;
    }

    @Override
    public boolean indexDocument(String index, String id, Map<String, Object> document) throws IOException {
        IndexRequest<Map<String, Object>> request = new IndexRequest.Builder<Map<String, Object>>()
                .index(index)
                .id(id)
                .document(document)
                .build();
        IndexResponse response = client.index(request);
        Result result = response.result();
        return result == Result.Created || result == Result.Updated;
    }

    @Override
    public boolean deleteDocument(String index, String id) throws IOException {
        DeleteRequest request = new DeleteRequest.Builder()
                .index(index)
                .id(id)
                .build();
        DeleteResponse response = client.delete(request);
        return response.result() == Result.Deleted;
    }

    @Override
    public List<String> getAllIndices() throws IOException {
        IndicesRequest request = new IndicesRequest.Builder().build();
        IndicesResponse response = client.cat().indices(request);
        List<String> indices = new ArrayList<>();
        for (IndicesRecord record : response.valueBody()) {
            indices.add(record.index());
        }
        return indices;
    }

    @Override
    public String getMapping(String index) throws IOException {
        GetMappingRequest request = new GetMappingRequest.Builder()
                .index(index)
                .build();
        GetMappingResponse response = client.indices().getMapping(request);
        IndexMappingRecord record = response.result().get(index);
        if (record != null && record.mappings() != null) {
            return record.mappings().toString();
        }
        return null;
    }

    @Override
    public List<String> getAllDocuments(String index) throws IOException {
        SearchRequest request = new SearchRequest.Builder()
                .index(index)
                .query(q -> q.matchAll(m -> m))
                .build();

        SearchResponse<JsonData> response = client.search(request, JsonData.class);
        List<String> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();  // Jackson object mapper

        for (Hit<JsonData> hit : response.hits().hits()) {
            // Convert JsonData to Map
            Map<String, Object> map = hit.source().to(Map.class);
            // Convert map to JSON string
            String json = mapper.writeValueAsString(map);
            results.add(json);
        }

        return results;
    }

    @Override
    public List<Map<String, Object>> searchProducts(String index, String query, Integer size) throws IOException {
        Query textQuery = buildTextQuery(query);
        return executeSearch(index, textQuery, size);
    }

    @Override
    public List<Map<String, Object>> searchProductsByGeohash(String index, String query, String geohash, Integer size) throws IOException {
        Query textQuery = buildTextQuery(query);
        Query geohashFilter = Query.of(q -> q.term(t -> t.field("geohash").value(v -> v.stringValue(geohash))));

        Query combined = Query.of(q -> q.bool(b -> b
                .must(textQuery)
                .filter(geohashFilter)));

        return executeSearch(index, combined, size);
    }

    private Query buildTextQuery(String query) {
        return Query.of(q -> q.multiMatch(mm -> mm
                .query(query)
                .fields(List.of(
                        "product_name^4",
                        "product_name.ngram^2",
                        "brand_name^2",
                        "brand_name.ngram",
                        "categories^2",
                        "categories.ngram",
                        "sub_categories",
                        "breadcrumbs",
                        "breadcrumbs.ngram",
                        "description",
                        "ingredients",
                        "ingredients.ngram"))
                .fuzziness("AUTO")));
    }

    private List<Map<String, Object>> executeSearch(String index, Query query, Integer size) throws IOException {
        SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index(index)
                .query(query)
                .minScore(10.0);  // Filter out weak matches (score < 10)

        if (size != null && size > 0) {
            requestBuilder.size(size);
        }

        SearchResponse<JsonData> response = client.search(requestBuilder.build(), JsonData.class);

        List<Map<String, Object>> results = new ArrayList<>();
        for (Hit<JsonData> hit : response.hits().hits()) {
            JsonData source = hit.source();
            if (source != null) {
                Map<String, Object> document = source.to(Map.class);
                Map<String, Object> enriched = new LinkedHashMap<>(document);
                enriched.put("_score", hit.score());
                results.add(enriched);
            }
        }

        return results;
    }

}
