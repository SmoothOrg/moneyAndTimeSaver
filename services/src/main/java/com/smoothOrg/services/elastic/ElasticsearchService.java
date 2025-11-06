package com.smoothOrg.services.elastic;

import java.io.IOException;
import java.util.Map;

public interface ElasticsearchService {
    boolean createIndex(String index) throws IOException;

    boolean updateMapping(String index, String mappingJson) throws IOException;

    String getDocument(String index, String id) throws IOException;

    boolean indexDocument(String index, String id, Map<String, Object> document) throws IOException;

    boolean deleteDocument(String index, String id) throws IOException;

    /**
     * Retrieve the names of all indices in the cluster.
     */
    java.util.List<String> getAllIndices() throws IOException;

    /**
     * Retrieve the mapping for the given index as a JSON string.
     */
    String getMapping(String index) throws IOException;

    /**
     * Retrieve all documents from the given index as JSON strings.
     */
    java.util.List<String> getAllDocuments(String index) throws IOException;
}
