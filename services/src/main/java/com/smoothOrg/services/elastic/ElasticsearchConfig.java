package com.smoothOrg.services.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.active:local}")
    private String active;

    // ---- Local (no auth) ----
    @Value("${elasticsearch.local.scheme:http}") private String localScheme;
    @Value("${elasticsearch.local.host:localhost}") private String localHost;
    @Value("${elasticsearch.local.port:9200}") private int localPort;

    // ---- Cloud (basic auth) ----
    @Value("${elasticsearch.cloud.scheme:https}") private String cloudScheme;
    @Value("${elasticsearch.cloud.host}") private String cloudHost;
    @Value("${elasticsearch.cloud.port:443}") private int cloudPort;
    @Value("${elasticsearch.cloud.username:}") private String cloudUser;
    @Value("${elasticsearch.cloud.password:}") private String cloudPass;

    // ---------- Builders ----------
    private RestClient buildNoAuth(String scheme, String host, int port) {
        RestClientBuilder b = RestClient.builder(new HttpHost(host, port, scheme));
        return b.build();
    }

    private RestClient buildBasicAuth(String scheme, String host, int port, String user, String pass) {
        RestClientBuilder b = RestClient.builder(new HttpHost(host, port, scheme));
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pass));
        b.setHttpClientConfigCallback(http -> http.setDefaultCredentialsProvider(provider));
        return b.build();
    }

    private ElasticsearchClient toEsClient(RestClient rc) {
        ElasticsearchTransport transport = new RestClientTransport(rc, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    // ---------- Named beans you can inject explicitly ----------
    @Bean(name = "esClientLocal")
    public ElasticsearchClient esClientLocal() {
        return toEsClient(buildNoAuth(localScheme, localHost, localPort));
    }

    @Bean(name = "esClientCloud")
    public ElasticsearchClient esClientCloud() {
        return toEsClient(buildBasicAuth(cloudScheme, cloudHost, cloudPort, cloudUser, cloudPass));
    }

    // ---------- Primary bean chosen by `elasticsearch.active` ----------
    @Bean
    @org.springframework.context.annotation.Primary
    public ElasticsearchClient elasticsearchClient(
            @Qualifier("esClientLocal") ElasticsearchClient local,
            @Qualifier("esClientCloud") ElasticsearchClient cloud) {
        return "cloud".equalsIgnoreCase(active) ? cloud : local;
    }
}
