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

    /**
     * Perform a text based product search across commonly used product fields.
     *
     * @param index the index to search
     * @param query the free-text query provided by the user
     * @param size  optional number of documents to return (defaults applied by caller)
     * @return the matching documents as maps containing their original fields
     */
    java.util.List<java.util.Map<String, Object>> searchProducts(String index, String query, Integer size) throws IOException;

    /**
     * Perform a text search for products limited to a specific geohash bucket.
     *
     * @param index   the index to search
     * @param query   the free-text query provided by the user
     * @param geohash the geohash code that should be matched
     * @param size    optional number of documents to return (defaults applied by caller)
     * @return the matching documents as maps containing their original fields
     */
    java.util.List<java.util.Map<String, Object>> searchProductsByGeohash(String index, String query, String geohash, Integer size) throws IOException;
}
