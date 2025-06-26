package com.smoothOrg.services.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import co.elastic.clients.json.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;

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
            return response.source().to(String.class);
        }
        return null;
    }
}
