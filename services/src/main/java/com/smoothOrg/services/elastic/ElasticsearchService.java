package com.smoothOrg.services.elastic;

import java.io.IOException;

public interface ElasticsearchService {
    boolean createIndex(String index) throws IOException;

    boolean updateMapping(String index, String mappingJson) throws IOException;

    String getDocument(String index, String id) throws IOException;
}
