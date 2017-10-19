package org.elasticsearch.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-10-18
 */
public class BaseDemo {

    protected TransportClient client;
    protected ElasticsearchTemplate elasticsearchTemplate;
    protected RestClient restClient ;

    @Before
    public void setUp() throws Exception {
        /**
         * 这里的连接方式指的是没有安装x-pack插件,如果安装了x-pack则参考{@link XPackBaseDemo}
         * 1. java客户端的方式是以tcp协议在9300端口上进行通信
         * 2. http客户端的方式是以http协议在9200端口上进行通信
         */
        client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        elasticsearchTemplate = new ElasticsearchTemplate(client);
        restClient = RestClient.builder(new HttpHost("localhost",9200)).build();
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
        analyzeRequest.text("中华人民共和国");
        ActionFuture<AnalyzeResponse> analyzeResponseActionFuture = client.admin().indices().analyze(analyzeRequest);
        List<AnalyzeResponse.AnalyzeToken> analyzeTokens =  analyzeResponseActionFuture.actionGet().getTokens();
        for (AnalyzeResponse.AnalyzeToken analyzeToken  : analyzeTokens){
            System.out.println(analyzeToken.getTerm());
        }
    }

    @Test
    public void testElasticsearchTemplateConnection() throws Exception {
        AnalyzeRequest analyzeRequest = new AnalyzeRequest();
        analyzeRequest.text("中华人民共和国");
        ActionFuture<AnalyzeResponse> analyzeResponseActionFuture =  elasticsearchTemplate.getClient().admin().indices().analyze(analyzeRequest);
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
        System.out.println(JSON.toJSONString(JSONObject.parse(EntityUtils.toString(response.getEntity())), SerializerFeature.PrettyFormat));
    }

    protected void println(SearchResponse searchResponse){
        SearchHit[]  searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits){
            System.out.println(JSON.toJSONString(searchHit.getSource(),SerializerFeature.PrettyFormat));
        }
    }

}
