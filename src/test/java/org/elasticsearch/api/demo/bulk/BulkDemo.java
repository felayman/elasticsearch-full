package org.elasticsearch.api.demo.bulk;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.Test;

import java.util.Date;

/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-docs-bulk.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class BulkDemo extends BaseDemo {

    @Test
    public void testForClient() throws Exception {
        BulkRequestBuilder bulkRequest = client.prepareBulk();

// either use client#prepare, or use Requests# to directly build index/delete requests
        bulkRequest.add(client.prepareIndex("twitter", "tweet", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("user", "kimchy")
                        .field("postDate", new Date())
                        .field("message", "trying out Elasticsearch")
                        .endObject()
                )
        );

        bulkRequest.add(client.prepareIndex("twitter", "tweet", "2")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("user", "kimchy")
                        .field("postDate", new Date())
                        .field("message", "another post")
                        .endObject()
                )
        );

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            // process failures by iterating through each bulk response item
        }
    }
}
