package org.elasticsearch.api.demo.get;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.api.demo.BaseDemo;
import org.junit.Test;

/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-docs-multi-get.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class MultiGetDemo extends BaseDemo {


    @Test
    public void name() throws Exception {
        MultiGetResponse multiGetItemResponses = client.prepareMultiGet()
                .add("twitter", "tweet", "1")
                .add("twitter", "tweet", "2", "3", "4")
                .add("another", "type", "foo")
                .get();
        for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
            GetResponse response = itemResponse.getResponse();
            if (response.isExists()) {
                String json = response.getSourceAsString();
            }
        }
    }
}
