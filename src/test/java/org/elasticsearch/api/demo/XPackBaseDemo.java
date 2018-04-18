package org.elasticsearch.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-04
 */
public class XPackBaseDemo {
    protected TransportClient client;
    protected RestClient restClient ;

    @Before
    public void setUp() throws Exception {
        /**
         * 如果es集群安装了x-pack插件则以此种方式连接集群
         * 1. java客户端的方式是以tcp协议在9300端口上进行通信
         * 2. http客户端的方式是以http协议在9200端口上进行通信
         */
        Settings settings = Settings.builder(). put("xpack.security.user", "elastic:changeme").build();
        client = new PreBuiltXPackTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"),9200));
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "changeme"));
        restClient = RestClient.builder(new HttpHost("localhost",9200,"http"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                }).build();
    }

    @After
    public void tearDown() throws Exception {
        if (client != null ){
            client.close();
        }

        if (restClient != null){
            restClient.close();
        }
    }

    @Test
    public void testClientConnection() throws Exception {
        AnalyzeRequest analyzeRequest = new AnalyzeRequest();
        analyzeRequest.text("美女");
        ActionFuture<AnalyzeResponse> analyzeResponseActionFuture = client.admin().indices().analyze(analyzeRequest);
        List<AnalyzeResponse.AnalyzeToken> analyzeTokens =  analyzeResponseActionFuture.actionGet().getTokens();
        for (AnalyzeResponse.AnalyzeToken analyzeToken  : analyzeTokens){
            System.out.println(analyzeToken.getTerm());
        }
    }


    @Test
    public void testRestClientConnection() throws Exception {
        String method = "GET";
        String endpoint = "/_analyze";
        Map<String, String> params = new HashMap<>();
        params.put("analyzer","standard");
        params.put("text","中华人民共和国");
        Response response = restClient.performRequest(method,endpoint,params);
        System.out.println(JSON.toJSONString(JSONObject.parse(EntityUtils.toString(response.getEntity())),SerializerFeature.PrettyFormat));
    }

    protected void println(SearchResponse searchResponse){
        SearchHit[]  searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits){
            System.out.println(JSON.toJSONString(searchHit.getSourceAsString(),SerializerFeature.PrettyFormat));
        }
    }
}
