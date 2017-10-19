package org.elasticsearch.api.demo.delete;

import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.junit.Test;

/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-docs-delete-by-query.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class DeleteByQueryDemo extends BaseDemo {

    @Test
    public void testForClient() throws Exception {
         DeleteByQueryAction.INSTANCE.newRequestBuilder(client).filter(QueryBuilders.termQuery("status",1)).execute().actionGet();
    }

}
