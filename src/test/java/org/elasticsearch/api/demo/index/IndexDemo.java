package org.elasticsearch.api.demo.index;

import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-docs-index.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class IndexDemo extends BaseDemo {


    /**
     * 使用json字符串来构造文档内容
     * @throws Exception
     */
    @Test
    public void testForUseStr() throws Exception {
        String json = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        client.prepareIndex("twitter", "tweet")
                .setSource(json, XContentType.JSON)
                .get();
    }


    /**
     * 使用map来构造文档内容
     * @throws Exception
     */
    @Test
    public void testForUseMap() throws Exception {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("user","kimchy");
        json.put("postDate",new Date());
        json.put("message","trying out Elasticsearch");
        client.prepareIndex("twitter", "tweet")
                .setSource(json, XContentType.JSON)
                .get();
    }

    /**
     * 使用elasticsearch官方提供的json构造器来构造文档内容
     * @throws Exception
     */
    @Test
    public void testForUseXContentBuilder() throws Exception {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("user", "kimchy")
                .field("postDate", new Date())
                .field("message", "trying out Elasticsearch")
                .endObject();
        client.prepareIndex("twitter", "tweet", "1")
                .setSource(builder)
                .get();
    }

}
